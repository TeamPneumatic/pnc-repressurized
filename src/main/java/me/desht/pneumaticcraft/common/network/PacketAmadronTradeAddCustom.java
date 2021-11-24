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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public PacketAmadronTradeAddCustom(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            AmadronPlayerOffer offer = getOffer();
            if (player == null) {
                handleClientSide(offer);
            } else {
                handleServerSide(player, offer);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleServerSide(ServerPlayerEntity player, AmadronPlayerOffer offer) {
        if (player.containerMenu instanceof ContainerAmadronAddTrade) {
            offer.updatePlayerId();
            if (AmadronOfferManager.getInstance().hasSimilarPlayerOffer(offer.getReversedOffer())) {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateReversedOffer"), false);
            } else if (AmadronOfferManager.getInstance().addPlayerOffer(offer)) {
                if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
                    NetworkHandler.sendToAll(this);
                }
                if (player.getMainHandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    ItemAmadronTablet.openGui(player, Hand.MAIN_HAND);
                } else if (player.getOffhandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    ItemAmadronTablet.openGui(player, Hand.OFF_HAND);
                }
            } else {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateOffer"), false);
            }
        }
    }

    private void handleClientSide(AmadronPlayerOffer offer) {
        if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
            ClientUtils.getClientPlayer().displayClientMessage(
                    new TranslationTextComponent("pneumaticcraft.message.amadron.playerAddedTrade",
                            offer.getVendorName(),
                            offer.getOutput().toString(),
                            offer.getInput().toString()
                    ), false);
        }
    }
}
