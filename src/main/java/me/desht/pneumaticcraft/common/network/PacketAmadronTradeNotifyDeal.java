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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: CLIENT
 * Sent by server to notify client of a trade deal made, or restock event
 * Using a packet rather than a simple chat message because the client can also elect not to receive
 * these notifications.
 */
public class PacketAmadronTradeNotifyDeal extends PacketAbstractAmadronTrade {
    private final int offerAmount;
    private final String buyingPlayer;

    public PacketAmadronTradeNotifyDeal(AmadronPlayerOffer offer, int offerAmount, String buyingPlayer) {
        super(offer);
        this.offerAmount = offerAmount;
        this.buyingPlayer = buyingPlayer;
    }

    public PacketAmadronTradeNotifyDeal(PacketBuffer buffer) {
        super(buffer);
        offerAmount = buffer.readInt();
        buyingPlayer = buffer.readUtf();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeInt(offerAmount);
        buf.writeUtf(buyingPlayer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (PNCConfig.Common.Amadron.notifyOfDealMade)
                ClientUtils.getClientPlayer().displayClientMessage(
                        xlate("pneumaticcraft.message.amadron.playerBought",
                                buyingPlayer,
                                offerAmount,
                                getOffer().getOutput().toString(),
                                getOffer().getInput().toString()
                        ), false
                );
        });
        ctx.get().setPacketHandled(true);
    }
}
