package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.common.item.ItemPneumaticBoots;
import me.desht.pneumaticcraft.common.item.ItemPneumaticLeggings;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventHandlerPneumaticArmor {

    private static final UUID PNEUMATIC_SPEED_ID = UUID.fromString("6ecaf25b-9619-4fd1-ae4c-c2f1521047d7");
    private static final AttributeModifier PNEUMATIC_SPEED_BOOST = (new AttributeModifier(PNEUMATIC_SPEED_ID, "Pneumatic speed boost", 0.3, 2)).setSaved(false);

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
            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.SHORT_HISS, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.7f, 0.8f, false), player.world);
            CommonHUDHandler.getHandlerForPlayer(player).useAir(stack, EntityEquipmentSlot.FEET, (int) -airNeeded);
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
            if (boots.getPressure(stack) > 0.1F) {
                player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
            } else {
                player.stepHeight = 0.6F;
            }
        } else {
            player.stepHeight = 0.6F;
        }

        if ((player.ticksExisted & 0x7) == 0) {
            // only check every 8 ticks, for performance reasons

            stack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            if (stack.getItem() instanceof ItemPneumaticLeggings && handler.isArmorReady(EntityEquipmentSlot.LEGS)) {
                ItemPneumaticLeggings legs = (ItemPneumaticLeggings) stack.getItem();
                if (legs.getPressure(stack) > 0.0F) {
                    IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                    if (attr.getModifier(PNEUMATIC_SPEED_ID) != null) {
                        attr.removeModifier(PNEUMATIC_SPEED_ID);
                    }
                    attr.applyModifier(PNEUMATIC_SPEED_BOOST);
                    if (checkMovement(player) && player.onGround && !player.isInsideOfMaterial(Material.WATER)) {
                        handler.useAir(stack, EntityEquipmentSlot.LEGS, -PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * 8);
                    }
                }
            } else {
                IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                if (attr.getModifier(PNEUMATIC_SPEED_ID) != null) {
                    attr.removeModifier(PNEUMATIC_SPEED_ID);
                }
            }

            moveMap.put(player.getName(), new Vec3d(player.posX, player.posY, player.posZ));
        }
    }

    private boolean checkMovement(EntityPlayer player) {
        Vec3d prev = moveMap.get(player.getName());
        if (prev == null) return false;
        return Math.abs(player.posX - prev.x) > 0.0001 || Math.abs(player.posZ - prev.z) > 0.0001;
    }
}
