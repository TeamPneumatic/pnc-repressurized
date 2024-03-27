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

import me.desht.pneumaticcraft.client.gui.AmadronScreen;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to confirm updated amount when player adjusts an offer with PacketAmadronOrderUpdate
 * (This needs a round trip rather than just updating client-side, since server needs to validate and cap
 *  requested shopping amounts)
 */
public record PacketAmadronOrderResponse(ResourceLocation offerId, int amount) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("amadron_order_response");

    public static PacketAmadronOrderResponse fromNetwork(FriendlyByteBuf buf) {
        return new PacketAmadronOrderResponse(buf. readResourceLocation(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(offerId);
        buf.writeVarInt(amount);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketAmadronOrderResponse message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Player player = ClientUtils.getClientPlayer();
            if (player.containerMenu instanceof AmadronMenu) {
                ((AmadronMenu) player.containerMenu).updateBasket(message.offerId(), message.amount());
                AmadronScreen.basketUpdated();
            }
        });
    }
}
