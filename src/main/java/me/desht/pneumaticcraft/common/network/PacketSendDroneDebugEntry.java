package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.debug.DroneDebugEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

/**
 * Received on: CLIENT
 * Sent by server to add a debug message to a debugged drone.
 */
public class PacketSendDroneDebugEntry extends PacketDroneDebugBase {
    private final DroneDebugEntry entry;

    public PacketSendDroneDebugEntry(DroneDebugEntry entry, IDroneBase drone) {
        super(drone);
        this.entry = entry;
    }

    public PacketSendDroneDebugEntry(PacketBuffer buffer) {
        super(buffer);
        entry = new DroneDebugEntry(buffer);
    }

    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        entry.toBytes(buf);
    }

    @Override
    void handle(PlayerEntity player, IDroneBase drone) {
        drone.getDebugger().addEntry(entry);
    }
}
