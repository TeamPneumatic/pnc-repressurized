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
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: BOTH
 * Sent by Amadron GUI when player is adding a custom player->player trade.
 * Sent by server to all clients to notify them of a trade addition
 */
public class PacketAmadronTradeAddCustom extends PacketAbstractAmadronTrade {
    public PacketAmadronTradeAddCustom(AmadronPlayerOffer offer) {
        super(offer);
    }

    public PacketAmadronTradeAddCustom(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            AmadronPlayerOffer offer = getOffer();
            if (player == null) {
                handleClientSide(offer);
            } else {
                handleServerSide(player, offer);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleServerSide(ServerPlayer player, AmadronPlayerOffer offer) {
        if (player.containerMenu instanceof ContainerAmadronAddTrade) {
            offer.updatePlayerId();
            if (AmadronOfferManager.getInstance().hasSimilarPlayerOffer(offer.getReversedOffer())) {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateReversedOffer"), false);
            } else if (AmadronOfferManager.getInstance().addPlayerOffer(offer)) {
                if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
                    NetworkHandler.sendToAll(this);
                }
                if (player.getMainHandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    ItemAmadronTablet.openGui(player, InteractionHand.MAIN_HAND);
                } else if (player.getOffhandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    ItemAmadronTablet.openGui(player, InteractionHand.OFF_HAND);
                }
            } else {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateOffer"), false);
            }
        }
    }

    private void handleClientSide(AmadronPlayerOffer offer) {
        if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
            ClientUtils.getClientPlayer().displayClientMessage(
                    new TranslatableComponent("pneumaticcraft.message.amadron.playerAddedTrade",
                            offer.getVendorName(),
                            offer.getOutput().toString(),
                            offer.getInput().toString()
                    ), false);
        }
    }
}
