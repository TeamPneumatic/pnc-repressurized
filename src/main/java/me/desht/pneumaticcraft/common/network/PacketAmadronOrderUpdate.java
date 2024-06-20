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

import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from client when an offer widget is clicked in the Amadron GUI to update the server-side order amount
 */
public record PacketAmadronOrderUpdate(ResourceLocation orderId, int mouseButton, boolean sneaking) implements CustomPacketPayload {
    public static final Type<PacketAmadronOrderUpdate> TYPE = new Type<>(RL("amadron_order_update"));

    public static final StreamCodec<FriendlyByteBuf, PacketAmadronOrderUpdate> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PacketAmadronOrderUpdate::orderId,
            ByteBufCodecs.VAR_INT, PacketAmadronOrderUpdate::mouseButton,
            ByteBufCodecs.BOOL, PacketAmadronOrderUpdate::sneaking,
            PacketAmadronOrderUpdate::new
    );

    @Override
    public Type<PacketAmadronOrderUpdate> type() {
        return TYPE;
    }

    public static void handle(PacketAmadronOrderUpdate message, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sp && sp.containerMenu instanceof AmadronMenu menu) {
            menu.clickOffer(message.orderId(), message.mouseButton(), message.sneaking(), sp);
        }
    }
}
