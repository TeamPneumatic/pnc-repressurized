package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketMultiHeader extends AbstractPacket<PacketMultiHeader> {
    private int length;
    private String className;
    private static final Map<UUID, PayloadBuffer> payloadBuffers = new HashMap<>();

    public PacketMultiHeader() {
    }

    public PacketMultiHeader(int length, String className) {
        this.length = length;
        this.className = className;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        length = buf.readInt();
        className = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(length);
        ByteBufUtils.writeUTF8String(buf, className);
    }

    @Override
    public void handleClientSide(PacketMultiHeader message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketMultiHeader message, EntityPlayer player) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) cl = PacketMultiHeader.class.getClassLoader(); // fallback
            Class<? extends AbstractPacket> clazz = (Class<? extends AbstractPacket>) cl.loadClass(className);
            payloadBuffers.put(player.getUniqueID(), new PayloadBuffer(clazz, length));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void receivePayload(EntityPlayer player, byte[] payload) {
        PayloadBuffer buffer = payloadBuffers.get(player.getUniqueID());
        if (buffer != null) {
            System.arraycopy(payload, 0, buffer.payload, buffer.offset, payload.length);
            buffer.offset += NetworkHandler.MAX_PAYLOAD_SIZE;
            if (buffer.offset > buffer.payload.length) {
                // we have the complete message
                try {
                    AbstractPacket packet = buffer.clazz.newInstance();
                    ByteBuf buf = Unpooled.wrappedBuffer(buffer.payload);
                    packet.fromBytes(buf);
                    packet.handleServerSide(packet, player);
                    payloadBuffers.remove(player.getUniqueID());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.error("Received unexpected multi-message payload from player " + player.getName() + " - " + player.getUniqueID());
        }
    }

    private static class PayloadBuffer {
        final Class<? extends AbstractPacket> clazz;
        final byte[] payload;
        int offset;

        PayloadBuffer(Class<? extends AbstractPacket> clazz, int length) {
            this.clazz = clazz;
            this.payload = new byte[length];
        }
    }
}
