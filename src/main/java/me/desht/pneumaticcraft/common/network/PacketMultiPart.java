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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Part of a multipart message from client (whole message too big to send at once)
 */
public class PacketMultiPart {
    private final byte[] payload;

    PacketMultiPart(byte[] payload) {
        this.payload = payload;
    }

    PacketMultiPart(FriendlyByteBuf buf) {
        payload = new byte[buf.readInt()];
        buf.readBytes(payload);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PacketMultiHeader.receivePayload(ctx.get().getSender(), payload));
        ctx.get().setPacketHandled(true);
    }
}
