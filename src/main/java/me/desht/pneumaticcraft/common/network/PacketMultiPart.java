package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketMultiPart extends AbstractPacket<PacketMultiPart> {
    byte[] payload;

    public PacketMultiPart() {
    }

    public PacketMultiPart(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        payload = new byte[buf.readInt()];
        buf.readBytes(payload);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    @Override
    public void handleClientSide(PacketMultiPart message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketMultiPart message, EntityPlayer player) {
        PacketMultiHeader.receivePayload(player, message.payload);
    }
}
