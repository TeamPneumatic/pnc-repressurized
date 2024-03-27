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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 *
 * Sent by client when player shift-scrolls the mouse wheel while holding an item implementing the
 * {@link me.desht.pneumaticcraft.common.item.IShiftScrollable} interface.
 */
public record PacketShiftScrollWheel(boolean forward, InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("shift_scroll_wheel");

    public static PacketShiftScrollWheel fromNetwork(FriendlyByteBuf buf) {
        return new PacketShiftScrollWheel(buf.readBoolean(), buf.readEnum(InteractionHand.class));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(forward);
        buf.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketShiftScrollWheel message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IShiftScrollable ss) {
                ss.onShiftScrolled(player, message.forward(), message.hand());
            }
        }));
    }
}
