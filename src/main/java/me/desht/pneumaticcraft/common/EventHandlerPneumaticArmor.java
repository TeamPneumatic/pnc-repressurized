package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.item.ItemPneumaticBoots;
import me.desht.pneumaticcraft.common.item.ItemPneumaticChestPlate;
import me.desht.pneumaticcraft.common.item.ItemPneumaticLeggings;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Events related to Pneumatic Armor.  Note any player-tick events are handled in CommonHUDHandler#tickArmorPiece()
 */
public class EventHandlerPneumaticArmor {
    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayer && event.getDistance() > 3.0F && !event.getEntity().world.isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!(stack.getItem() instanceof ItemPneumaticBoots)) {
                return;
            }
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (!handler.isArmorEnabled()) return;

            ItemPneumaticBoots boots = (ItemPneumaticBoots) stack.getItem();
            float airNeeded = event.getDistance() * PneumaticValues.PNEUMATIC_ARMOR_FALL_USAGE;
            float airAvailable = boots.getVolume(stack) * handler.getArmorPressure(EntityEquipmentSlot.FEET);
            if (airAvailable < 1) {
                return;
            } else if (airAvailable > airNeeded) {
                event.setCanceled(true);
            } else {
                event.setDamageMultiplier(1.0F - (airAvailable / airNeeded));
            }
            for (int i = 0; i < event.getDistance() / 3; i++) {
                float sx = player.getRNG().nextFloat() * 2F - 1F;
                float sz = player.getRNG().nextFloat() * 2F - 1F;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, player.posX, player.posY, player.posZ, sx, 0.2, sz), player.world);
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
            if (armorStack.getItem() instanceof ItemPneumaticChestPlate && event.getSource().isFireDamage()) {
                CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.getArmorPressure(EntityEquipmentSlot.CHEST) > 0.1F && handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.SECURITY) > 0) {
                    event.setCanceled(true);
                    player.extinguish();
                    if (!player.world.isRemote) {
                        handler.addAir(armorStack, EntityEquipmentSlot.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
                        for (int i = 0; i < 2; i++) {
                            float sx = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            float sz = player.getRNG().nextFloat() * 1.5F - 0.75F;
                            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, player.posX + sx, player.posY + 1, player.posZ + sz, sx / 2, -0.5, sz / 2), player.world);
                        }
                        if ((player.ticksExisted & 0xf) == 0) {
                            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.LEAKING_GAS_SOUND, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.5f, 0.7f, false), player.world);
                            tryExtinguish(player);
                        }
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
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (!handler.isJetBootsActive() && stack.getItem() instanceof ItemPneumaticLeggings && handler.isArmorReady(EntityEquipmentSlot.LEGS) && handler.isJumpBoostEnabled()) {
                ItemPneumaticLeggings legs = (ItemPneumaticLeggings) stack.getItem();
                if (legs.getPressure(stack) > 0.01F) {
                    int rangeUpgrades = handler.getUpgradeCount(EntityEquipmentSlot.LEGS, IItemRegistry.EnumUpgrade.RANGE,
                            player.isSneaking() ? 1 : PneumaticValues.PNEUMATIC_LEGS_MAX_JUMP);
                    player.motionY += rangeUpgrades * 0.15;
                    float f = player.rotationYaw * 0.017453292F;
                    float m = player.isSprinting() ? 0.25F * rangeUpgrades : 0.15F * rangeUpgrades;
                    if (player.motionX != 0) player.motionX -= (double)(MathHelper.sin(f) * m);
                    if (player.motionZ != 0) player.motionZ += (double)(MathHelper.cos(f) * m);
                    player.fallDistance -= rangeUpgrades * 1.5;
                    handler.addAir(stack, EntityEquipmentSlot.LEGS, -PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * rangeUpgrades * (player.isSprinting() ? 2 : 1));
                }
            }
        }
    }
}
