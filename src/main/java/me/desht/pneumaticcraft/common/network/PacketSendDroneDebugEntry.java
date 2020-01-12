package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.living.DebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to add a debug message to a debugged drone.
 */
public class PacketSendDroneDebugEntry {
    private DebugEntry entry;
    private int entityId;

    public PacketSendDroneDebugEntry() {
    }

    public PacketSendDroneDebugEntry(DebugEntry entry, EntityDrone drone) {
        this.entry = entry;
        entityId = drone.getEntityId();
    }

    public PacketSendDroneDebugEntry(PacketBuffer buffer) {
        entry = new DebugEntry(buffer);
        entityId = buffer.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        entry.toBytes(buf);
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtils.getClientWorld().getEntityByID(entityId);
            if (entity instanceof EntityDrone) {
                EntityDrone drone = (EntityDrone) entity;
                drone.addDebugEntry(entry);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
