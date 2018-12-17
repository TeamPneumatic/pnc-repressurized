package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class PacketChestplateLauncher extends AbstractPacket<PacketChestplateLauncher> {
    private static final float SCALE_FACTOR = 0.7f;

    private float amount;

    public PacketChestplateLauncher() {
    }

    public PacketChestplateLauncher(float amount) {
        this.amount = amount;
    }

    @Override
    public void handleClientSide(PacketChestplateLauncher message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketChestplateLauncher message, EntityPlayer player) {
        World world = player.getEntityWorld();
        ItemStack stack = player.getHeldItemOffhand();
        CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
        int upgrades = handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.DISPENSER, 4);

        if (handler.getArmorPressure(EntityEquipmentSlot.CHEST) > 0.1f && handler.isArmorReady(EntityEquipmentSlot.CHEST) && upgrades > 0 && !stack.isEmpty()) {
            ItemStack toFire = player.capabilities.isCreativeMode ? ItemHandlerHelper.copyStackWithSize(stack, 1) : stack.splitStack(1);
            Entity launchedEntity = TileEntityAirCannon.getEntityToLaunch(player.getEntityWorld(), toFire, player,true, true);

            if (launchedEntity instanceof EntityArrow) {
                EntityArrow arrow = (EntityArrow) launchedEntity;
                arrow.pickupStatus = player.capabilities.isCreativeMode ? EntityArrow.PickupStatus.CREATIVE_ONLY : EntityArrow.PickupStatus.ALLOWED;
                arrow.setDamage(arrow.getDamage() + 0.25 * upgrades * message.amount);
            }

            Vec3d velocity = player.getLookVec().normalize().scale(message.amount * upgrades * SCALE_FACTOR);
            TileEntityAirCannon.launchEntity(launchedEntity, player.getPositionEyes(1f).add(0, -0.1, 0), velocity, true);

            int usedAir = (int) (20 * upgrades * message.amount);
            if (!player.capabilities.isCreativeMode) {
                handler.addAir(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), EntityEquipmentSlot.CHEST, -usedAir);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(amount);
    }
}
