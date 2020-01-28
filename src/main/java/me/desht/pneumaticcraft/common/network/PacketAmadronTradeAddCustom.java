package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
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

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddCustom() {
    }

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
        offer.updatePlayerId();
        if (AmadronOfferManager.getInstance().hasSimilarPlayerOffer(offer.getReversedOffer())) {
            player.sendStatusMessage(xlate("message.amadron.duplicateReversedOffer"), false);
        } else if (AmadronOfferManager.getInstance().addPlayerOffer(offer)) {
            if (PNCConfig.Common.Amadron.notifyOfTradeAddition) {
                NetworkHandler.sendToAll(this);
            }
        } else {
            player.sendStatusMessage(xlate("message.amadron.duplicateOffer"), false);
        }
    }

    private void handleClientSide(AmadronPlayerOffer offer) {
        if (PNCConfig.Common.Amadron.notifyOfTradeAddition) {
            ClientUtils.getClientPlayer().sendStatusMessage(
                    new TranslationTextComponent("message.amadron.playerAddedTrade",
                            offer.getVendor(),
                            offer.getOutput().toString(),
                            offer.getInput().toString()
                    ), false);
        }
    }
}
