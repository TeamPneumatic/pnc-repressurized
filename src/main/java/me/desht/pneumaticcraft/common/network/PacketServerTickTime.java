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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to keep client appraised of server TPS
 * Helps keep systems which use dead reckoning (right now, just Elevator) to stay as much in sync with the server
 * as possible
 */
public record PacketServerTickTime(double tickTime) implements CustomPacketPayload {
    public static final Type<PacketServerTickTime> TYPE = new Type<>(RL("server_tick_time"));

    public static final StreamCodec<FriendlyByteBuf, PacketServerTickTime> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, PacketServerTickTime::tickTime,
            PacketServerTickTime::new
    );

    private static double tickTimeMultiplier = 1;

    @Override
    public Type<PacketServerTickTime> type() {
        return TYPE;
    }

    public static void handle(PacketServerTickTime message, IPayloadContext ignoredCtx) {
        tickTimeMultiplier = Math.min(1, 50D / Math.max(message.tickTime(), 0.01));
    }

    public static double getTickTimeMultiplier() {
        return tickTimeMultiplier;
    }
}
