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

import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.AmadronAddTradeMenu;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: BOTH
 * Sent by Amadron GUI when player is adding a custom player->player trade.
 * Sent by server to all clients to notify them of a trade addition
 */
public record PacketAmadronTradeAddCustom(AmadronPlayerOffer offer) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("amadron_add_custom_trade");

    public static PacketAmadronTradeAddCustom fromNetwork(FriendlyByteBuf buffer) {
        return new PacketAmadronTradeAddCustom(AmadronPlayerOffer.playerOfferFromBuf(buffer));
    }

    public static void handle(PacketAmadronTradeAddCustom message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (ctx.flow().isClientbound()) {
                message.handleClientSide(player);
            } else if (player instanceof ServerPlayer sp) {
                message.handleServerSide(sp);
            }
        }));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        offer.write(buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    private void handleClientSide(Player player) {
        if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
            player.displayClientMessage(
                    Component.translatable("pneumaticcraft.message.amadron.playerAddedTrade",
                            offer.getVendorName(),
                            offer.getOutput().toString(),
                            offer.getInput().toString()
                    ), false);
        }
    }

    private void handleServerSide(ServerPlayer player) {
        if (player.containerMenu instanceof AmadronAddTradeMenu) {
            offer.updatePlayerId();
            if (AmadronOfferManager.getInstance().hasSimilarPlayerOffer(offer.getReversedOffer())) {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateReversedOffer"), false);
            } else if (AmadronOfferManager.getInstance().addPlayerOffer(offer)) {
                if (ConfigHelper.common().amadron.notifyOfTradeAddition.get()) {
                    NetworkHandler.sendToAll(this);
                }
                if (player.getMainHandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    AmadronTabletItem.openGui(player, InteractionHand.MAIN_HAND);
                } else if (player.getOffhandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
                    AmadronTabletItem.openGui(player, InteractionHand.OFF_HAND);
                }
            } else {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.duplicateOffer"), false);
            }
        }
    }
}
