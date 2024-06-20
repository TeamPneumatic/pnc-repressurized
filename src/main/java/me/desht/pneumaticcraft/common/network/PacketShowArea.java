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

import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to make a block entity render its area of effect
 */
public record PacketShowArea(BlockPos pos, Set<BlockPos> area) implements CustomPacketPayload {
    public static final Type<PacketShowArea> TYPE = new Type<>(RL("show_area"));

    public static final StreamCodec<FriendlyByteBuf, PacketShowArea> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketShowArea::pos,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), PacketShowArea::area,
            PacketShowArea::new
    );

    public static PacketShowArea forPos(BlockPos pos) {
        return new PacketShowArea(pos, Set.of());
    }

    public static PacketShowArea forArea(BlockPos pos, Set<BlockPos> area) {
        return new PacketShowArea(pos, area);
    }

    @Override
    public Type<PacketShowArea> type() {
        return TYPE;
    }

    public static void handle(PacketShowArea message, IPayloadContext ctx) {
        AreaRenderManager.getInstance().showArea(message.area(), 0x9000FFFF, ClientUtils.getBlockEntity(message.pos()));
    }
}
