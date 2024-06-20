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

import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 *
 * Sent by client when player shift-scrolls the mouse wheel while holding an item implementing the
 * {@link me.desht.pneumaticcraft.common.item.IShiftScrollable} interface.
 */
public record PacketShiftScrollWheel(boolean forward, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<PacketShiftScrollWheel> TYPE = new Type<>(RL("shift_scroll_wheel"));

    public static final StreamCodec<FriendlyByteBuf, PacketShiftScrollWheel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketShiftScrollWheel::forward,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketShiftScrollWheel::hand,
            PacketShiftScrollWheel::new
    );

    @Override
    public Type<PacketShiftScrollWheel> type() {
        return TYPE;
    }

    public static void handle(PacketShiftScrollWheel message, IPayloadContext ctx) {
        if (ctx.player().getItemInHand(message.hand()).getItem() instanceof IShiftScrollable ss) {
            ss.onShiftScrolled(ctx.player(), message.forward(), message.hand());
        }
    }
}
