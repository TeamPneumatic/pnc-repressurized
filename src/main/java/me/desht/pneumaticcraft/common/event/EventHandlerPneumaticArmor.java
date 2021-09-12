package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsStateSync;
import me.desht.pneumaticcraft.common.network.PacketSendArmorHUDMessage;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker.JetBootsState;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
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
    private static final Map<UUID, Map<String, Integer>> targetWarnings = new HashMap<>();

    private static final int ARMOR_REPAIR_AMOUNT = 16;  // durability repaired per compressed iron ingot

    @SubscribeEvent
    public void onMobTargetSet(LivingSetAttackTargetEvent event) {
        // Helmet with entity tracker upgrade warns player if a mob targets them.
        // LivingSetAttackTargetEvent gets continuously fired even if the mob was already targeting the same target
        // so we need to track locally what is targeting whom, and only warn the player if the mob is newly
        // targeting them - otherwise, massive spam.
        int mobId = event.getEntityLiving().getId();
        if (event.getTarget() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getTarget();
            if (isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)) {
                if (!targetingTracker.containsKey(mobId) || targetingTracker.get(mobId) != event.getTarget().getId()) {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().entityTrackerHandler, true)) {
                        Map<String, Integer> map = targetWarnings.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
                        map.merge(event.getEntityLiving().getName().getString(), 1, Integer::sum);
                    }
                }
            }
            targetingTracker.put(mobId, event.getTarget().getId());
        } else {
            targetingTracker.remove(mobId);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        targetingTracker.remove(event.getEntityLiving().getId());
    }

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (event.getDistance() > 3.0F && event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            ItemStack stack = player.getItemBySlot(EquipmentSlotType.FEET);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
            if (event.getEntity().level.getDifficulty() == Difficulty.HARD && jbState.isActive()) {
                // thrusting into the ground hurts at hard difficulty!
                event.setDamageMultiplier(0.2F);
                return;
            }
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().jumpBoostHandler, true)) {
                // straight fall distance reduction if jump upgrade operational in legs
                event.setDistance(Math.max(0, event.getDistance() - 1.5f * handler.getUpgradeCount(EquipmentSlotType.LEGS, EnumUpgrade.JUMPING)));
                if (event.getDistance() < 2) {
                    event.setCanceled(true);
                    return;
                }
            }

            if (!player.level.isClientSide) {
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
                    float sx = player.getRandom().nextFloat() * 0.6F - 0.3F;
                    float sz = player.getRandom().nextFloat() * 0.6F - 0.3F;
                    NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, player.getX(), player.getY(), player.getZ(), sx, 0.1, sz), player.level, player.blockPosition());
                }
                player.level.playSound(null, player.blockPosition(), ModSounds.SHORT_HISS.get(), SoundCategory.PLAYERS, 0.3f, 0.8f);
                handler.addAir(EquipmentSlotType.FEET, (int) -airNeeded);
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            if (isPneumaticArmorPiece(player, EquipmentSlotType.CHEST) && event.getSource().isFire() && !(player.isCreative() || player.isSpectator())) {
                // security upgrade in chestplate protects from fire
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.hasMinPressure(EquipmentSlotType.CHEST) && handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.SECURITY) > 0) {
                    event.setCanceled(true);
                    player.clearFire();
                    if (!player.level.isClientSide) {
                        handler.addAir(EquipmentSlotType.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
                        for (int i = 0; i < 2; i++) {
                            float sx = player.getRandom().nextFloat() * 1.5F - 0.75F;
                            float sz = player.getRandom().nextFloat() * 1.5F - 0.75F;
                            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, player.getX() + sx, player.getY() + 1, player.getZ() + sz, sx / 4, -0.2, sz / 4), player.level, player.blockPosition());
                        }
                        if ((player.tickCount & 0xf) == 0) {
                            player.level.playSound(null, player.blockPosition(), ModSounds.LEAKING_GAS.get(), SoundCategory.PLAYERS, 1f, 0.7f);
                            tryExtinguish(player);
                        }
                    }
                }
            }
        }
    }

    private void tryExtinguish(PlayerEntity player) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = player.blockPosition().offset(i, 0, j);
                BlockState state = player.level.getBlockState(pos);
                if (state.getBlock() == Blocks.FIRE && player.getRandom().nextInt(3) == 0) {
                    player.level.removeBlock(pos, false);
                } else if ((state.getBlock() == Blocks.LAVA) && player.getRandom().nextInt(5) == 0) {
                    int level = state.getValue(FlowingFluidBlock.LEVEL);
                    player.level.setBlockAndUpdate(pos, level == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
                }
            }
        }
    }

    /**
     * Jump boost due to jump upgrades in leggings
     */
    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            ItemStack stack = player.getItemBySlot(EquipmentSlotType.LEGS);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().jetBootsHandler, true)
                    && !handler.getExtensionData(ArmorUpgradeRegistry.getInstance().jetBootsHandler).isSmartHover()) {
                // enabled jet boots = no jumping
                return;
            }
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().jumpBoostHandler, true)) {
                float power = ItemPneumaticArmor.getIntData(stack, ItemPneumaticArmor.NBT_JUMP_BOOST, 100, 0, 100) / 100.0f;
                int rangeUpgrades = handler.getUpgradeCount(EquipmentSlotType.LEGS, EnumUpgrade.JUMPING,
                        player.isShiftKeyDown() ? 1 : PneumaticValues.PNEUMATIC_LEGS_MAX_JUMP);
                float actualBoost = Math.max(1.0f, rangeUpgrades * power);
                Vector3d m = player.getDeltaMovement();
                double scale = player.isSprinting() ? actualBoost : actualBoost * 0.6;
                player.setDeltaMovement(m.x * scale, m.y + actualBoost * 0.15f, m.z * scale);
                int airUsed = (int) Math.ceil(PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * actualBoost * (player.isSprinting() ? 2 : 1));
                handler.addAir(EquipmentSlotType.LEGS, -airUsed);
            }
        }
    }

    /**
     * Allow the player to dig at improved speed if flying with builder mode active
     * (need tier 5 upgrade for normal dig speed)
     */
    @SubscribeEvent
    public void breakSpeedCheck(PlayerEvent.BreakSpeed event) {
        PlayerEntity player = event.getPlayer();
        int max = PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES;
        if (isPneumaticArmorPiece(player, EquipmentSlotType.FEET) && !player.isOnGround()) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(event.getPlayer());
            JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
            if (jbState.isEnabled() && jbState.isBuilderMode()) {
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
                if (handler.hasMinPressure(EquipmentSlotType.FEET) && handler.isArmorReady(EquipmentSlotType.FEET)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Client-side: play particles for all (close enough) player entities with enabled jet boots, including the actual player.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (event.player.level.isClientSide) {
            handleJetbootsPose(event.player);
        } else if (event.player instanceof ServerPlayerEntity
                && event.player.level.getGameTime() % 20 == 0
                && ItemPneumaticArmor.isPneumaticArmorPiece(event.player, EquipmentSlotType.HEAD)) {
            handleTargetWarnings((ServerPlayerEntity) event.player);
        }
    }

    private void handleJetbootsPose(PlayerEntity thisPlayer) {
        JetBootsStateTracker tracker = JetBootsStateTracker.getClientTracker();
        int distThresholdSq = ClientUtils.getRenderDistanceThresholdSq();
        for (PlayerEntity otherPlayer : thisPlayer.level.players()) {
            if (!otherPlayer.isOnGround() && isPneumaticArmorPiece(otherPlayer, EquipmentSlotType.FEET)) {
                JetBootsState state = tracker.getJetBootsState(otherPlayer);
                if (state != null && state.isEnabled() && (!otherPlayer.isFallFlying() || state.isActive()) && otherPlayer.distanceToSqr(thisPlayer) < distThresholdSq) {
                    // note: particles now played in MovingSoundJetboots
                    if (otherPlayer.getId() != thisPlayer.getId() && state.shouldRotatePlayer()) {
                        otherPlayer.setPose(Pose.FALL_FLYING);
                    }
                }
            }
        }
    }

    private void handleTargetWarnings(ServerPlayerEntity player) {
        Map<String, Integer> map = targetWarnings.get(player.getUUID());
        if (map != null) {
            map.forEach((name, count) -> {
                TranslationTextComponent msg = xlate("pneumaticcraft.armor.message.targetWarning", name);
                if (count > 1) msg.append(" (x" + count + ")");
                NetworkHandler.sendToPlayer(new PacketSendArmorHUDMessage(msg, 60, 0x70FF4000), player);
            });
            map.clear();
        }
    }

    @SubscribeEvent
    public void onArmorRepair(AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof ItemPneumaticArmor
                && PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON.contains(event.getRight().getItem()))
        {
            ItemStack repairedItem = event.getLeft().copy();
            int damageRepaired = Math.min(repairedItem.getDamageValue(), event.getRight().getCount() * ARMOR_REPAIR_AMOUNT);
            int ingotsToUse = ((damageRepaired - 1)/ ARMOR_REPAIR_AMOUNT) + 1;
            repairedItem.setDamageValue(repairedItem.getDamageValue() - damageRepaired);

            event.setOutput(repairedItem);
            event.setCost(Math.max(1, ingotsToUse / 2));
            event.setMaterialCost(ingotsToUse);
        }
    }

    @SubscribeEvent
    public void onPlayerTrack(PlayerEvent.StartTracking event) {
        // keep other players up to date with the state of each player's jetboots activity
        if (event.getPlayer() instanceof ServerPlayerEntity && event.getTarget() instanceof ServerPlayerEntity) {
            ServerPlayerEntity trackedPlayer = (ServerPlayerEntity) event.getTarget();
            if (trackedPlayer.getItemBySlot(EquipmentSlotType.FEET).getItem() == ModItems.PNEUMATIC_BOOTS.get()) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(trackedPlayer);
                if (handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) > 0) {
                    JetBootsState state = JetBootsStateTracker.getServerTracker().getJetBootsState(trackedPlayer);
                    NetworkHandler.sendToPlayer(new PacketJetBootsStateSync(trackedPlayer, state), (ServerPlayerEntity) event.getPlayer());
                }
            }
        }
    }
}
