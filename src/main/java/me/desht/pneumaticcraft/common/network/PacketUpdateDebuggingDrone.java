package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class PacketUpdateDebuggingDrone extends AbstractPacket<PacketUpdateDebuggingDrone> {

    private int entityId;

    public PacketUpdateDebuggingDrone() {

    }

    public PacketUpdateDebuggingDrone(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void handleClientSide(PacketUpdateDebuggingDrone message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketUpdateDebuggingDrone message, EntityPlayer player) {
        ItemStack stack = player.inventory.armorItemInSlot(3);
        if (stack != null) {
            NBTUtil.setInteger(stack, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE, message.entityId);
            Entity entity = player.world.getEntityByID(message.entityId);
            if (entity instanceof EntityDrone) {
                ((EntityDrone) entity).trackAsDebugged((EntityPlayerMP) player);
            }
        }
    }

}
