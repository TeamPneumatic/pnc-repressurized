package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.entity.living.DebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSendDroneDebugEntry extends AbstractPacket<PacketSendDroneDebugEntry> {
    private DebugEntry entry;
    private int entityId;

    public PacketSendDroneDebugEntry() {
    }

    public PacketSendDroneDebugEntry(DebugEntry entry, EntityDrone drone) {
        this.entry = entry;
        entityId = drone.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entry = new DebugEntry(buf);
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        entry.toBytes(buf);
        buf.writeInt(entityId);
    }

    @Override
    public void handleClientSide(PacketSendDroneDebugEntry message, EntityPlayer player) {
        Entity entity = player.world.getEntityByID(message.entityId);
        if (entity instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) entity;
            drone.addDebugEntry(message.entry);
        }
    }

    @Override
    public void handleServerSide(PacketSendDroneDebugEntry message, EntityPlayer player) {

    }

}
