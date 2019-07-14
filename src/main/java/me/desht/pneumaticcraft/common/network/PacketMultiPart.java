package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Part of a multipart mesage from client (whole message too big to send at once)
 */
public class PacketMultiPart {
    private byte[] payload;

    public PacketMultiPart() {
    }

    PacketMultiPart(byte[] payload) {
        this.payload = payload;
    }

    PacketMultiPart(PacketBuffer buf) {
        payload = new byte[buf.readInt()];
        buf.readBytes(payload);
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PacketMultiHeader.receivePayload(ctx.get().getSender(), payload));
        ctx.get().setPacketHandled(true);
    }
}
