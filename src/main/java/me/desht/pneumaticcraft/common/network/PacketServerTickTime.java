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
 * Received on: CLIENT
 * Sent by server to keep client appraised of server TPS
 * Helps keep systems which use dead reckoning (right now, just Elevator) to stay as much in sync with the server
 * as possible
 */
public class PacketServerTickTime {
    public static double tickTimeMultiplier = 1;

    private final double tickTime;

    public PacketServerTickTime(double tickTime) {
        this.tickTime = tickTime;
    }

    public PacketServerTickTime(FriendlyByteBuf buffer) {
        this.tickTime = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(tickTime);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> tickTimeMultiplier = Math.min(1, 50D / Math.max(tickTime, 0.01)));
        ctx.get().setPacketHandled(true);
    }
}
