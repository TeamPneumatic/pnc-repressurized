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

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Part of a multipart message from client (whole message too big to send at once)
 */
public record PacketMultiPart(byte[] payload) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("multipart");

    public static PacketMultiPart fromNetwork(FriendlyByteBuf buf) {
        int len = buf.readInt();
        byte[] payload = Util.make(new byte[len], buf::readBytes);

        return new PacketMultiPart(payload);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketMultiPart message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() ->
                PacketMultiHeader.receivePayload(player, message.payload())
        ));
    }
}
