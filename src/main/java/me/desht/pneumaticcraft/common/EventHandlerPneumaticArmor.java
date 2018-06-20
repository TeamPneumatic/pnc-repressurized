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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventHandlerPneumaticArmor {

    private static final UUID PNEUMATIC_SPEED_ID[] = {
            UUID.fromString("6ecaf25b-9619-4fd1-ae4c-c2f1521047d7"),
            UUID.fromString("091a3128-1fa9-4f03-8e30-8848d370caa2"),
            UUID.fromString("8dd25db8-102e-4960-aeb0-36417d200957")
    };
    private static final AttributeModifier PNEUMATIC_SPEED_BOOST[] = new AttributeModifier[3];
    static {
        PNEUMATIC_SPEED_BOOST[0] = (new AttributeModifier(PNEUMATIC_SPEED_ID[0], "Pneumatic speed boost", 0.25, 2)).setSaved(false);
        PNEUMATIC_SPEED_BOOST[1] = (new AttributeModifier(PNEUMATIC_SPEED_ID[1], "Pneumatic speed boost", 0.5, 2)).setSaved(false);
        PNEUMATIC_SPEED_BOOST[2] = (new AttributeModifier(PNEUMATIC_SPEED_ID[2], "Pneumatic speed boost", 0.75, 2)).setSaved(false);
    }

    // track player movement across ticks on the server
    private static final Map<String,Vec3d> moveMap = new HashMap<>();

    @SubscribeEvent
    public void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayer && event.getDistance() > 3.0F && !event.getEntity().world.isRemote) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!(stack.getItem() instanceof ItemPneumaticBoots)) {
                return;
            }

            ItemPneumaticBoots boots = (ItemPneumaticBoots) stack.getItem();

            float airNeeded = event.getDistance() * PneumaticValues.PNEUMATIC_ARMOR_FALL_USAGE;
            float airAvailable = boots.getVolume(stack) * boots.getPressure(stack);
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
            CommonHUDHandler.getHandlerForPlayer(player).useAir(stack, EntityEquipmentSlot.FEET, (int) -airNeeded);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            ItemStack armorStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (armorStack.getItem() instanceof ItemPneumaticChestPlate && event.getSource().isFireDamage()) {
                CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
                if (handler.getArmorPressure(EntityEquipmentSlot.CHEST) > 0.1F && handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.SECURITY) > 0) {
                    event.setCanceled(true);
                    player.extinguish();
                    if (!player.world.isRemote) {
                        handler.useAir(armorStack, EntityEquipmentSlot.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
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
                } else if (state.getBlock() == Blocks.LAVA && player.getRNG().nextInt(15) == 0) {
                    player.world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                } else if (state.getBlock() == Blocks.FLOWING_LAVA && player.getRNG().nextInt(15) == 0) {
                    player.world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        EntityPlayer player = event.player;

        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
        if (stack.getItem() instanceof ItemPneumaticBoots && handler.isArmorReady(EntityEquipmentSlot.FEET)) {
            ItemPneumaticBoots boots = (ItemPneumaticBoots) stack.getItem();
            if (boots.getPressure(stack) > 0.1F && handler.isStepAssistEnabled()) {
                player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
            } else {
                player.stepHeight = 0.6F;
            }
        } else {
            player.stepHeight = 0.6F;
        }

        if (!player.world.isRemote && (player.ticksExisted & 0xf) == 0) {
            // only check every 16 ticks, for performance reasons

            removeSpeedModifiers(player);
            stack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            if (stack.getItem() instanceof ItemPneumaticLeggings && handler.isArmorReady(EntityEquipmentSlot.LEGS) && handler.isRunSpeedEnabled()) {
                int speedUpgrades = Math.min(3, handler.getUpgradeCount(EntityEquipmentSlot.LEGS, IItemRegistry.EnumUpgrade.SPEED));
                ItemPneumaticLeggings legs = (ItemPneumaticLeggings) stack.getItem();
                if (legs.getPressure(stack) > 0.0F && speedUpgrades > 0) {
                    IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                    attr.applyModifier(PNEUMATIC_SPEED_BOOST[speedUpgrades - 1]);
                    if (checkMovement(player) && player.onGround && !player.isInsideOfMaterial(Material.WATER)) {
                        handler.useAir(stack, EntityEquipmentSlot.LEGS, -PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * 16 * speedUpgrades);
                    }
                }
            }

            moveMap.put(player.getName(), new Vec3d(player.posX, player.posY, player.posZ));
        }
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (stack.getItem() instanceof ItemPneumaticLeggings && handler.isArmorReady(EntityEquipmentSlot.LEGS) && handler.isJumpBoostEnabled()) {
                ItemPneumaticLeggings legs = (ItemPneumaticLeggings) stack.getItem();
                if (legs.getPressure(stack) > 0.1F) {
                    int rangeUpgrades = Math.min(6, handler.getUpgradeCount(EntityEquipmentSlot.LEGS, IItemRegistry.EnumUpgrade.RANGE));
                    if (player.isSneaking()) rangeUpgrades = Math.min(1, rangeUpgrades);
                    player.motionY += rangeUpgrades * 0.15;
                    player.fallDistance -= rangeUpgrades * 1.5;
                    handler.useAir(stack, EntityEquipmentSlot.LEGS, -PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * rangeUpgrades);
                }
            }
        }
    }

    private void removeSpeedModifiers(EntityPlayer player) {
        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        for (int i = 0; i < 3; i++) {
            if (attr.getModifier(PNEUMATIC_SPEED_ID[i]) != null) {
                attr.removeModifier(PNEUMATIC_SPEED_ID[i]);
            }
        }
    }

    private boolean checkMovement(EntityPlayer player) {
        Vec3d prev = moveMap.get(player.getName());
        if (prev == null) return false;
        return Math.abs(player.posX - prev.x) > 0.0001 || Math.abs(player.posZ - prev.z) > 0.0001;
    }
}
