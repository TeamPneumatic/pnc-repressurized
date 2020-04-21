package me.desht.pneumaticcraft.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to keep client appraised of server TPS
 * Helps keep systems which use dead reckoning (right now, just Elevator) to stay as much in sync with the server
 * as possible
 */
public class PacketServerTickTime {
    private double tickTime;
    public static double tickTimeMultiplier = 1;

    public PacketServerTickTime() {
    }

    public PacketServerTickTime(double tickTime) {
        this.tickTime = tickTime;
    }

    public PacketServerTickTime(PacketBuffer buffer) {
        this.tickTime = buffer.readDouble();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeDouble(tickTime);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> tickTimeMultiplier = Math.min(1, 50D / Math.max(tickTime, 0.01)));
        ctx.get().setPacketHandled(true);
    }
}
