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

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: CLIENT
 * Sent by server to notify client of a trade deal made, or restock event
 * Using a packet rather than a simple chat message because the client can also elect not to receive
 * these notifications.
 */
public record PacketAmadronTradeNotifyDeal(AmadronPlayerOffer offer, int offerAmount, String buyingPlayer) implements CustomPacketPayload {
    public static final Type<PacketAmadronTradeNotifyDeal> TYPE = new Type<>(RL("amadron_notify_deal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketAmadronTradeNotifyDeal> STREAM_CODEC = StreamCodec.composite(
            AmadronPlayerOffer.STREAM_CODEC, PacketAmadronTradeNotifyDeal::offer,
            ByteBufCodecs.INT, PacketAmadronTradeNotifyDeal::offerAmount,
            ByteBufCodecs.STRING_UTF8, PacketAmadronTradeNotifyDeal::buyingPlayer,
            PacketAmadronTradeNotifyDeal::new
    );

    @Override
    public Type<PacketAmadronTradeNotifyDeal> type() {
        return TYPE;
    }

    public static void handle(PacketAmadronTradeNotifyDeal message, IPayloadContext ctx) {
            if (ConfigHelper.common().amadron.notifyOfDealMade.get())
                ctx.player().displayClientMessage(
                        xlate("pneumaticcraft.message.amadron.playerBought",
                                message.buyingPlayer(),
                                message.offerAmount(),
                                message.offer().getOutput().toString(),
                                message.offer().getInput().toString()
                        ), false
                );
    }
}
