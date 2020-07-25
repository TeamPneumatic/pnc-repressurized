package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSendArmorHUDMessage;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.item.ItemPneumaticArmor.isPneumaticArmorPiece;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Events related to Pneumatic Armor.  Note any player-tick events are handled in CommonHUDHandler#tickArmorPiece()
 */
public class EventHandlerPneumaticArmor {
    private static final Map<Integer, Integer> targetingTracker = new HashMap<>();

    private static final Map<UUID,Long> armorJumping = new HashMap<>();
    private static final int ARMOR_REPAIR_AMOUNT = 16;  // durability repaired per compressed iron ingot

    @SubscribeEvent
    public void onMobTargetSet(LivingSetAttackTargetEvent event) {
        // Helmet with entity tracker upgrade warns player if a mob targets them.
        // LivingSetAttackTargetEvent gets continuously fired even if the mob was already targeting the same target
        // so we need to track locally what is targeting whom, and only warn the player if the mob is newly
        // targeting them - otherwise, massive spam.
        int mobId = event.getEntityLiving().getEntityId();
        if (event.getTarget() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getTarget();
            if (isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)) {
                if (!targetingTracker.containsKey(mobId) || targetingTracker.get(mobId) != event.getTarget().getEntityId()) {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.isArmorReady(EquipmentSlotType.HEAD) && handler.getArmorPressure(EquipmentSlotType.HEAD) > 0 && handler.isEntityTrackerEnabled()) {
                        NetworkHandler.sendToPlayer(new PacketSendArmorHUDMessage(
                                xlate("pneumaticcraft.armor.message.targetWarning", event.getEntityLiving().getName().getString()),
                                        60, 0x70FF4000),
                                player
                        );
                    }
                }
            }
            targetingTracker.put(mobId, event.getTarget().getEntityId());
        } else {
            targetingTracker.remove(mobId);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        targetingTracker.remove(event.getEntityLiving().getEntityId());
    }

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        // todo handle this client side too to avoid sending particle/sound packets over the network?
        if (!(event.getEntity() instanceof PlayerEntity) || event.getEntity().world.isRemote) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();

        // this is a kludge, but setting player.fallDistance to a negative amount doesn't seem to work as it should
        // cancel fall damage if the player jumped with pneumatic legs in the last 40 ticks
        long when = armorJumping.getOrDefault(player.getUniqueID(), 0L);
        if (player.world.getGameTime() - when < 40) {
            event.setCanceled(true);
            return;
        }
        armorJumping.remove(player.getUniqueID());

        if (event.getDistance() > 3.0F) {
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (!handler.isArmorEnabled()) return;
            if (event.getEntity().world.getDifficulty() == Difficulty.HARD && handler.isJetBootsActive()) {
                event.setDamageMultiplier(0.2F);
                return;  // thrusting into the ground hurts at hard difficulty!
            }

            float airNeeded = event.getDistance() * PneumaticValues.PNEUMATIC_ARMOR_FALL_USAGE;
            int vol = stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(IAirHandler::getVolume).orElse(0);
            float airAvailable = vol * handler.getArmorPressure(EquipmentSlotType.FEET);
            if (airAvailable < 1) {
                return;
            } else if (airAvailable >= airNeeded) {
                event.setCanceled(true);
            } else {
                event.setDamageMultiplier(1.0F - (airAvailable / airNeeded));
            }
            for (int i = 0; i < event.getDistance() / 2; i++) {
                float sx = player.getRNG().nextFloat() * 0.6F - 0.3F;
                float sz = player.getRNG().nextFloat() * 0.6F - 0.3F;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, player.getPosX(), player.getPosY(), player.getPosZ(), sx, 0.1, sz), player.world);
            }
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.SHORT_HISS.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 0.3f, 0.8f, false), player.world);
            handler.addAir(EquipmentSlotType.FEET, (int) -airNeeded);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (isPneumaticArmorPiece(player, EquipmentSlotType.CHEST) && event.getSource().isFireDamage()) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.getArmorPressure(EquipmentSlotType.CHEST) > 0.1F && handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.SECURITY) > 0) {
                    event.setCanceled(true);
                    player.extinguish();
                    if (!player.world.isRemote) {
                        handler.addAir(EquipmentSlotType.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
                        for (int i = 0; i < 2; i++) {
                            float sx = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            float sz = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, player.getPosX() + sx, player.getPosY() + 1, player.getPosZ() + sz, sx / 4, -0.2, sz / 4), player.world);
                        }
                        if ((player.ticksExisted & 0xf) == 0) {
                            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.LEAKING_GAS.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 0.5f, 0.7f, false), player.world);
                            tryExtinguish(player);
                        }
                    }
                }
            } else if (event.getSource() instanceof EntityDamageSource
                    && ((EntityDamageSource) event.getSource()).getIsThornsDamage()
                    && event.getSource().getTrueSource() instanceof GuardianEntity) {
                // not actually armor-related, but it's the right event...
                // don't take thorns damage from Guardians when attacking with minigun
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ItemMinigun) {
                    Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
                    if (minigun != null && minigun.getMinigunSpeed() >= Minigun.MAX_GUN_SPEED) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private void tryExtinguish(PlayerEntity player) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = player.getPosition().add(i, 0, j);
                BlockState state = player.world.getBlockState(pos);
                if (state.getBlock() == Blocks.FIRE && player.getRNG().nextInt(3) == 0) {
                    player.world.removeBlock(pos, false);
                } else if ((state.getBlock() == Blocks.LAVA) && player.getRNG().nextInt(5) == 0) {
                    int level = state.get(FlowingFluidBlock.LEVEL);
                    player.world.setBlockState(pos, level == 0 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.COBBLESTONE.getDefaultState());
                }
            }
        }
    }

    /**
     * Jump boost due to leggings range upgrades
     */
    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (!handler.isJetBootsEnabled() && handler.isArmorReady(EquipmentSlotType.LEGS)
                    && handler.isJumpBoostEnabled() && handler.getArmorPressure(EquipmentSlotType.LEGS) > 0.01F) {
                float power = ItemPneumaticArmor.getIntData(stack, ItemPneumaticArmor.NBT_JUMP_BOOST, 100, 0, 100) / 100.0f;
                int rangeUpgrades = handler.getUpgradeCount(EquipmentSlotType.LEGS, EnumUpgrade.JUMPING,
                        player.isSneaking() ? 1 : PneumaticValues.PNEUMATIC_LEGS_MAX_JUMP);
                float actualBoost = Math.max(1.0f, rangeUpgrades * power);
                float scale = player.isSprinting() ? 0.3f * actualBoost : 0.225f * actualBoost;
                float rotRad = player.rotationYaw * 0.017453292f;  // deg2rad
                Vector3d m = player.getMotion();
                double addX = m.x == 0 ? 0 : - (double)(MathHelper.sin(rotRad) * scale);
                double addZ = m.z == 0 ? 0 : + (double)(MathHelper.cos(rotRad) * scale);
                player.setMotion(m.x + addX, m.y + actualBoost * 0.15f, m.z + addZ);

                armorJumping.put(player.getUniqueID(), player.world.getGameTime());
                int airUsed = (int) Math.ceil(PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * actualBoost * (player.isSprinting() ? 2 : 1));
                handler.addAir(EquipmentSlotType.LEGS, -airUsed);
            }
        }
    }

    /**
     * Allow the player to dig at improved speed if flying with builder mode active
     * (need 10 upgrades for normal dig speed)
     */
    @SubscribeEvent
    public void breakSpeedCheck(PlayerEvent.BreakSpeed event) {
        PlayerEntity player = event.getPlayer();
        int max = PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES;
        if (isPneumaticArmorPiece(player, EquipmentSlotType.FEET)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(event.getPlayer());
            if (handler.isJetBootsEnabled() && !player.isOnGround() && handler.isJetBootsBuilderMode()) {
                int n = (max + 1) - handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS, max);
                if (n < 4) {
                    float mult = 5.0f / n;   // default dig speed when not on ground is 1/5 of normal
                    float oldSpeed = event.getOriginalSpeed();
                    float newSpeed = event.getNewSpeed();
                    if (oldSpeed < newSpeed * mult) {
                        event.setNewSpeed(newSpeed * mult);
                    }
                }
            }
        }
    }

    /**
     * Prevent farmland trampling with pneumatic boots
     */
    @SubscribeEvent
    public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (isPneumaticArmorPiece(player, EquipmentSlotType.FEET)) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.getArmorPressure(EquipmentSlotType.FEET) > 0 && handler.isArmorReady(EquipmentSlotType.FEET)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static final Vector3d IDLE_VEC = new Vector3d(0, -0.5, -0);

    /**
     * Client side: play particles for all known players (including us) with active jet boots either idling or firing
     */
    @SubscribeEvent
    public void playJetbootsParticles(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote) {
            JetBootsStateTracker tracker = JetBootsStateTracker.getTracker(event.player);
            for (PlayerEntity player : event.player.world.getPlayers()) {
                if (!player.isOnGround() && isPneumaticArmorPiece(player, EquipmentSlotType.FEET)) {
                    JetBootsStateTracker.JetBootsState state = tracker.getJetBootsState(player);
                    if (state != null && state.isEnabled()) {
                        int nParticles = state.isActive() ? 5 : 1;
                        Vector3d jetVec = state.shouldRotatePlayer() ? player.getLookVec().scale(-0.5) : IDLE_VEC;
                        Vector3d feet = getFeetPos(player, state.shouldRotatePlayer());
                        for (int i = 0; i < nParticles; i++) {
                            player.world.addParticle(AirParticleData.DENSE, feet.x, feet.y, feet.z, jetVec.x, jetVec.y, jetVec.z);
                        }
                    }
                }
            }
        }
    }

    private Vector3d getFeetPos(PlayerEntity player, boolean rotated) {
        if (!rotated) return player.getPositionVec();

        double midY = (player.getPosY() + player.getEyePosition(1.0f).y) / 2;
        return player.getPositionVec().add(player.getLookVec().scale(player.getPosY() - midY));
    }

    @SubscribeEvent
    public void onArmorRepair(AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof ItemPneumaticArmor
                && PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON.contains(event.getRight().getItem()))
        {
            ItemStack repairedItem = event.getLeft().copy();
            int damageRepaired = Math.min(repairedItem.getDamage(), event.getRight().getCount() * ARMOR_REPAIR_AMOUNT);
            int ingotsToUse = ((damageRepaired - 1)/ ARMOR_REPAIR_AMOUNT) + 1;
            repairedItem.setDamage(repairedItem.getDamage() - damageRepaired);

            event.setOutput(repairedItem);
            event.setCost(Math.max(1, ingotsToUse / 2));
            event.setMaterialCost(ingotsToUse);
        }
    }
}
