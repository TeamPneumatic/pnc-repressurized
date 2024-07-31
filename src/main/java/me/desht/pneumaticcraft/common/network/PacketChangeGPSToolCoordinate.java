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

import me.desht.pneumaticcraft.common.item.IGPSToolSync;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public record PacketChangeGPSToolCoordinate(BlockPos pos, InteractionHand hand, String variable, int index, boolean activeIndex)
        implements CustomPacketPayload {
    public static final Type<PacketChangeGPSToolCoordinate> TYPE = new Type<>(RL("change_gps_tool_coord"));

    public static final StreamCodec<FriendlyByteBuf, PacketChangeGPSToolCoordinate> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketChangeGPSToolCoordinate::pos,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketChangeGPSToolCoordinate::hand,
            ByteBufCodecs.STRING_UTF8, PacketChangeGPSToolCoordinate::variable,
            ByteBufCodecs.VAR_INT, PacketChangeGPSToolCoordinate::index,
            ByteBufCodecs.BOOL, PacketChangeGPSToolCoordinate::activeIndex,
            PacketChangeGPSToolCoordinate::new
    );

    @Override
    public Type<PacketChangeGPSToolCoordinate> type() {
        return TYPE;
    }

    public static void handle(PacketChangeGPSToolCoordinate message, IPayloadContext ctx) {
        ItemStack stack = ctx.player().getItemInHand(message.hand);
        if (stack.getItem() instanceof IGPSToolSync sync) {
            sync.syncFromClient(ctx.player(), stack, message.index, message.pos, message.variable, message.activeIndex);
        }
    }
}
