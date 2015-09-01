package pneumaticCraft.common.network;

import java.io.IOException;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.common.config.AmadronOfferSettings;
import pneumaticCraft.common.config.AmadronOfferStaticConfig;
import pneumaticCraft.common.recipes.AmadronOfferCustom;
import pneumaticCraft.common.recipes.AmadronOfferManager;

public class PacketAmadronTradeAdd extends PacketAbstractAmadronTrade<PacketAmadronTradeAdd>{

    public PacketAmadronTradeAdd(){}

    public PacketAmadronTradeAdd(AmadronOfferCustom offer){
        super(offer);
    }

    @Override
    public void handleClientSide(PacketAmadronTradeAdd message, EntityPlayer player){
        if(AmadronOfferSettings.notifyOfTradeAddition) player.addChatMessage(new ChatComponentText(I18n.format("message.amadron.playerAddedTrade", message.getOffer().getVendor(), WidgetAmadronOffer.getStringForObject(message.getOffer().getOutput()), WidgetAmadronOffer.getStringForObject(message.getOffer().getInput()))));
    }

    @Override
    public void handleServerSide(PacketAmadronTradeAdd message, EntityPlayer player){
        AmadronOfferCustom offer = message.getOffer();
        offer.updatePlayerId();
        if(AmadronOfferManager.getInstance().hasOffer(offer.copy().invert())) {
            player.addChatMessage(new ChatComponentTranslation("message.amadron.duplicateReversedOffer"));
        } else if(AmadronOfferManager.getInstance().addStaticOffer(offer)) {
            if(AmadronOfferSettings.notifyOfTradeAddition) NetworkHandler.sendToAll(message);
            try {
                AmadronOfferStaticConfig.INSTANCE.writeToFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            player.addChatMessage(new ChatComponentTranslation("message.amadron.duplicateOffer"));
        }
    }
}
