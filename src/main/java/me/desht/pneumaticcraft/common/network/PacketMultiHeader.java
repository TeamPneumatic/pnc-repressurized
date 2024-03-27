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
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * First part of a following multipart message from the client
 */
public record PacketMultiHeader(int length, String className) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("multi_header");
    private static final Map<UUID, PayloadBuffer> payloadBuffers = new HashMap<>();

    public static PacketMultiHeader fromNetwork(FriendlyByteBuf buffer) {
        return new PacketMultiHeader(buffer.readInt(), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeUtf(className);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketMultiHeader message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) cl = PacketMultiHeader.class.getClassLoader(); // fallback
                Class<?> clazz = cl.loadClass(message.className());
                payloadBuffers.put(player.getUUID(), new PayloadBuffer(clazz.asSubclass(ILargePayload.class), message.length()));
            } catch (ClassNotFoundException|ClassCastException e) {
                e.printStackTrace();
            }
        }));
    }

    static void receivePayload(@Nonnull Player player, byte[] payload) {
        PayloadBuffer buffer = payloadBuffers.get(player.getUUID());
        if (buffer != null) {
            System.arraycopy(payload, 0, buffer.payload, buffer.offset, payload.length);
            buffer.offset += ILargePayload.MAX_PAYLOAD_SIZE;
            if (buffer.offset > buffer.payload.length) {
                // we have the complete message
                try {
                    Constructor<? extends ILargePayload> ctor = buffer.clazz.getConstructor(FriendlyByteBuf.class);
                    ILargePayload packet = ctor.newInstance(new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer.payload)));
                    packet.handleLargePayload(player);
                    payloadBuffers.remove(player.getUUID());
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
