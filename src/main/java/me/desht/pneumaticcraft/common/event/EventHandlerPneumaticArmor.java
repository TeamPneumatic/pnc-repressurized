package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSendArmorHUDMessage;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Events related to Pneumatic Armor.  Note any player-tick events are handled in CommonHUDHandler#tickArmorPiece()
 */
public class EventHandlerPneumaticArmor {
    private static final Map<Integer, Integer> targetingTracker = new HashMap<>();

    @SubscribeEvent
    public void onMobTargetSet(LivingSetAttackTargetEvent event) {
        // Helmet with entity tracker upgrade warns player if a mob targets them.
        // LivingSetAttackTargetEvent gets continuously fired even if the mob was already targeting the same target
        // so we need to track locally what is targeting whom, and only warn the player if the mob is newly
        // targeting them - otherwise, massive spam.
        int mobId = event.getEntityLiving().getEntityId();
        if (event.getTarget() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getTarget();
            if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemPneumaticArmor) {
                if (!targetingTracker.containsKey(mobId) || targetingTracker.get(mobId) != event.getTarget().getEntityId()) {
                    CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
                    if (handler.isArmorReady(EntityEquipmentSlot.HEAD) && handler.getArmorPressure(EntityEquipmentSlot.HEAD) > 0 && handler.isEntityTrackerEnabled()) {
                        NetworkHandler.sendTo(new PacketSendArmorHUDMessage(
                                "pneumaticHelmet.message.targetWarning", 60, 0x70FF4000, event.getEntityLiving().getName()),
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
        if (event.getEntity() instanceof EntityPlayer && event.getDistance() > 3.0F && !event.getEntity().world.isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (!handler.isArmorEnabled()) return;
            if (event.getEntity().world.getDifficulty() == EnumDifficulty.HARD && handler.isJetBootsActive()) {
                event.setDamageMultiplier(0.2F);
                return;  // thrusting into the ground hurts at hard difficulty!
            }

            ItemPneumaticArmor boots = (ItemPneumaticArmor) stack.getItem();
            float airNeeded = event.getDistance() * PneumaticValues.PNEUMATIC_ARMOR_FALL_USAGE;
            float airAvailable = boots.getVolume(stack) * handler.getArmorPressure(EntityEquipmentSlot.FEET);
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
                NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumCustomParticleType.AIR_PARTICLE_DENSE, player.posX, player.posY, player.posZ, sx, 0.1, sz), player.world);
            }
            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.SHORT_HISS, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.3f, 0.8f, false), player.world);
            handler.addAir(stack, EntityEquipmentSlot.FEET, (int) -airNeeded);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            ItemStack armorStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (armorStack.getItem() instanceof ItemPneumaticArmor && event.getSource().isFireDamage()) {
                CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.getArmorPressure(EntityEquipmentSlot.CHEST) > 0.1F && handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.SECURITY) > 0) {
                    event.setCanceled(true);
                    player.extinguish();
                    if (!player.world.isRemote) {
                        handler.addAir(armorStack, EntityEquipmentSlot.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
                        for (int i = 0; i < 2; i++) {
                            float sx = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            float sz = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumCustomParticleType.AIR_PARTICLE_DENSE, player.posX + sx, player.posY + 1, player.posZ + sz, sx / 4, -0.2, sz / 4), player.world);
                        }
                        if ((player.ticksExisted & 0xf) == 0) {
                            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.LEAKING_GAS_SOUND, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.5f, 0.7f, false), player.world);
                            tryExtinguish(player);
                        }
                    }
                }
            } else if (event.getSource() instanceof EntityDamageSource
                    && ((EntityDamageSource) event.getSource()).getIsThornsDamage()
                    && event.getSource().getTrueSource() instanceof EntityGuardian) {
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

    private void tryExtinguish(EntityPlayer player) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = player.getPosition().add(i, 0, j);
                IBlockState state = player.world.getBlockState(pos);
                if (state.getBlock() == Blocks.FIRE && player.getRNG().nextInt(3) == 0) {
                    player.world.setBlockToAir(pos);
                } else if ((state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA) && player.getRNG().nextInt(5) == 0) {
                    for (IProperty prop : state.getPropertyKeys()) {
                        if (prop.getName().equals("level")) {
                            PropertyInteger iProp = (PropertyInteger) prop;
                            int level = state.getValue(iProp);
                            player.world.setBlockState(pos, level == 0 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.COBBLESTONE.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            if (!(stack.getItem() instanceof ItemPneumaticArmor)) {
                return;
            }
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (!handler.isJetBootsActive() && handler.isArmorReady(EntityEquipmentSlot.LEGS)
                    && handler.isJumpBoostEnabled() && handler.getArmorPressure(EntityEquipmentSlot.LEGS) > 0.01F) {
                float power = ItemPneumaticArmor.getIntData(stack, "jumpBoost", 100) / 100.0f;
                int rangeUpgrades = handler.getUpgradeCount(EntityEquipmentSlot.LEGS, IItemRegistry.EnumUpgrade.RANGE,
                        player.isSneaking() ? 1 : PneumaticValues.PNEUMATIC_LEGS_MAX_JUMP);
                float actualBoost = Math.max(1.0f, rangeUpgrades * power);
                player.motionY += actualBoost * 0.15f;
                float rotRad = player.rotationYaw * 0.017453292f;  // deg2rad
                float scale = player.isSprinting() ? 0.25f * actualBoost : 0.15f * actualBoost;
                if (player.motionX != 0) player.motionX -= (double)(MathHelper.sin(rotRad) * scale);
                if (player.motionZ != 0) player.motionZ += (double)(MathHelper.cos(rotRad) * scale);
                player.fallDistance -= actualBoost * 1.5;
                int airUsed = (int) Math.ceil(PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * actualBoost * (player.isSprinting() ? 2 : 1));
                handler.addAir(stack, EntityEquipmentSlot.LEGS, -airUsed);
            }
        }
    }
}
