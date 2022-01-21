/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * First part of a following multi-part message from the client
 */
public class PacketMultiHeader {
    private final int length;
    private final String className;
    private static final Map<UUID, PayloadBuffer> payloadBuffers = new HashMap<>();

    PacketMultiHeader(int length, String className) {
        this.length = length;
        this.className = className;
    }

    PacketMultiHeader(FriendlyByteBuf buffer) {
        length = buffer.readInt();
        className = buffer.readUtf(32767);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeUtf(className);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) cl = PacketMultiHeader.class.getClassLoader(); // fallback
                Class<?> clazz = cl.loadClass(className);
                UUID id = ctx.get().getSender() == null ? ClientUtils.getClientPlayer().getUUID() : ctx.get().getSender().getUUID();
                payloadBuffers.put(id, new PayloadBuffer(clazz.asSubclass(ILargePayload.class), length));
            } catch (ClassNotFoundException|ClassCastException e) {
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    static void receivePayload(Player player, byte[] payload) {
        UUID id = player == null ? ClientUtils.getClientPlayer().getUUID() : player.getUUID();
        PayloadBuffer buffer = payloadBuffers.get(id);
        if (buffer != null) {
            System.arraycopy(payload, 0, buffer.payload, buffer.offset, payload.length);
            buffer.offset += ILargePayload.MAX_PAYLOAD_SIZE;
            if (buffer.offset > buffer.payload.length) {
                // we have the complete message
                try {
                    Constructor<? extends ILargePayload> ctor = buffer.clazz.getConstructor(FriendlyByteBuf.class);
                    ILargePayload packet = ctor.newInstance(new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer.payload)));
                    packet.handleLargePayload(player);
                    payloadBuffers.remove(id);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.error("Received unexpected multi-message payload from player " + player.getName() + " - " + player.getUUID());
        }
    }

    private static class PayloadBuffer {
        final Class<? extends ILargePayload> clazz;
        final byte[] payload;
        int offset;

        PayloadBuffer(Class<? extends ILargePayload> clazz, int length) {
            this.clazz = clazz;
            this.payload = new byte[length];
        }
    }
}
