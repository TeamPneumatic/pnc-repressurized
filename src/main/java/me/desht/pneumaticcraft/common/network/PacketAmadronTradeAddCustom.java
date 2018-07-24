package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class PacketAmadronTradeAddCustom extends PacketAbstractAmadronTrade<PacketAmadronTradeAddCustom> {

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddCustom() {
    }

    public PacketAmadronTradeAddCustom(AmadronOfferCustom offer) {
        super(offer);
    }

    @Override
    public void handleClientSide(PacketAmadronTradeAddCustom message, EntityPlayer player) {
        if (AmadronOfferSettings.notifyOfTradeAddition)
            player.sendStatusMessage(new TextComponentTranslation("message.amadron.playerAddedTrade",
                    message.getOffer().getVendor(),
                    WidgetAmadronOffer.getStringForObject(message.getOffer().getOutput()),
                    WidgetAmadronOffer.getStringForObject(message.getOffer().getInput())), false);
    }

    @Override
    public void handleServerSide(PacketAmadronTradeAddCustom message, EntityPlayer player) {
        AmadronOfferCustom offer = message.getOffer();
        offer.updatePlayerId();
        if (AmadronOfferManager.getInstance().hasOffer(offer.copy().invert())) {
            player.sendStatusMessage(new TextComponentTranslation("message.amadron.duplicateReversedOffer"), false);
        } else if (AmadronOfferManager.getInstance().addStaticOffer(offer)) {
            if (AmadronOfferSettings.notifyOfTradeAddition) NetworkHandler.sendToAll(message);
            try {
                AmadronOfferStaticConfig.INSTANCE.writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            player.sendStatusMessage(new TextComponentTranslation("message.amadron.duplicateOffer"), false);
        }
    }
}
