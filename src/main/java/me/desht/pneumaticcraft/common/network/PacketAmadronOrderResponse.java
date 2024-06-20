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
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to confirm updated amount when player adjusts an offer with PacketAmadronOrderUpdate
 * (This needs a round trip rather than just updating client-side, since server needs to validate and cap
 *  requested shopping amounts)
 */
public record PacketAmadronOrderResponse(ResourceLocation offerId, int amount) implements CustomPacketPayload {
    public static final Type<PacketAmadronOrderResponse> TYPE = new Type<>(RL("amadron_order_response"));

    public static final StreamCodec<FriendlyByteBuf, PacketAmadronOrderResponse> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PacketAmadronOrderResponse::offerId,
            ByteBufCodecs.VAR_INT, PacketAmadronOrderResponse::amount,
            PacketAmadronOrderResponse::new
    );

    @Override
    public Type<PacketAmadronOrderResponse> type() {
        return TYPE;
    }

    public static void handle(PacketAmadronOrderResponse message, IPayloadContext ctx) {
        if (ctx.player().containerMenu instanceof AmadronMenu am) {
            am.updateBasket(message.offerId(), message.amount());
            AmadronScreen.basketUpdated();
        }
    }
}
