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

import me.desht.pneumaticcraft.common.item.ILeftClickableItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when certain items are left-clicked in the air (which is a client-only event)
 */
public enum PacketLeftClickEmpty implements CustomPacketPayload {
    INSTANCE;

    public static final ResourceLocation ID = RL("left_click_empty");

    @SuppressWarnings("EmptyMethod")
    public static PacketLeftClickEmpty fromNetwork(@SuppressWarnings("unused") FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void write(@SuppressWarnings("unused") FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(@SuppressWarnings("unused") PacketLeftClickEmpty message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player instanceof ServerPlayer sp && sp.getMainHandItem().getItem() instanceof ILeftClickableItem lc) {
                lc.onLeftClickEmpty(sp);
            }
        }));
    }
}
