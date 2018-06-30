package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticBoots;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class PacketPneumaticKick extends AbstractPacket<PacketPneumaticKick> {
    public PacketPneumaticKick() {
    }

    @Override
    public void handleClientSide(PacketPneumaticKick message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketPneumaticKick message, EntityPlayer player) {
        if (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPneumaticBoots) {
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (handler.isArmorEnabled()) {
                int upgrades = handler.getUpgradeCount(EntityEquipmentSlot.FEET, IItemRegistry.EnumUpgrade.DISPENSER);
                if (upgrades > 0) {
                    handleKick(player, Math.min(PneumaticValues.PNEUMATIC_KICK_MAX_UPGRADES, upgrades));
                }
            }
        }
    }

    private void handleKick(EntityPlayer player, int upgrades) {
        Vec3d lookVec = player.getLookVec().normalize();

        double playerFootY = player.posY - player.height / 4;
        AxisAlignedBB box = new AxisAlignedBB(player.posX, playerFootY, player.posZ, player.posX, playerFootY, player.posZ)
                .grow(1.0, 1.0, 1.0).offset(lookVec);
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, box);
        if (entities.isEmpty()) return;
        entities.sort(Comparator.comparingDouble(o -> o.getDistanceSq(player)));

        Entity target = entities.get(0);
        if (target instanceof EntityLiving) {
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), 3.0f + upgrades * 0.5f);
        }
        lookVec = lookVec.scale(1.0 + upgrades * 0.5f);
        target.motionX += lookVec.x;
        target.motionY += lookVec.y;
        target.motionZ += lookVec.z;

        NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.PUNCH, SoundCategory.PLAYERS, target.posX, target.posY, target.posZ, 1.0f, 1.0f, false), player.world);
        NetworkHandler.sendToAllAround(new PacketSetEntityMotion(target, target.motionX, target.motionY, target.motionZ), player.world);
        NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.EXPLOSION_LARGE, target.posX, target.posY, target.posZ, 1.0D, 0.0D, 0.0D), player.world);
        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        CommonHUDHandler.getHandlerForPlayer(player).addAir(boots, EntityEquipmentSlot.FEET, -PneumaticValues.PNEUMATIC_KICK_AIR_USAGE * (2 << upgrades));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
