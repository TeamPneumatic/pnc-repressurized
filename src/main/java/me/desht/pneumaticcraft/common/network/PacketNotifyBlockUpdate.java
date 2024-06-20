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

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 *
 * Sent by server when a block is dropped by shift-wrenching it, rotated by wrenching it, or if a pneumatic BE explodes
 * due to overpressure.
 * This happens server-side (block updates are triggered on the server), but the client needs to know too so that
 * neighbouring cached block shapes (pressure tubes especially, but potentially anything) can be recalculated.
 */
public record PacketNotifyBlockUpdate(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketNotifyBlockUpdate> TYPE = new Type<>(RL("notify_block_update"));

    public static final StreamCodec<FriendlyByteBuf, PacketNotifyBlockUpdate> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketNotifyBlockUpdate::pos,
            PacketNotifyBlockUpdate::new
    );

    @Override
    public Type<PacketNotifyBlockUpdate> type() {
        return TYPE;
    }

    public static void handle(PacketNotifyBlockUpdate message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        level.getBlockState(message.pos())
                .updateNeighbourShapes(level, message.pos(), Block.UPDATE_ALL);
    }
}
