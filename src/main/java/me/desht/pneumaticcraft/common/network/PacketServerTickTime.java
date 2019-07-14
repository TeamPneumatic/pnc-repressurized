package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to keep client appraised of server TPS
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

    public void toBytes(ByteBuf buffer) {
        buffer.writeDouble(tickTime);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            tickTimeMultiplier = Math.min(1, 50D / Math.max(tickTime, 0.01));
        });
        ctx.get().setPacketHandled(true);
    }
}
