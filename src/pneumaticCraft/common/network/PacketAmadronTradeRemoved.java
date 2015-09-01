package pneumaticCraft.common.network;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.common.config.AmadronOfferSettings;
import pneumaticCraft.common.recipes.AmadronOfferCustom;

public class PacketAmadronTradeRemoved extends PacketAbstractAmadronTrade<PacketAmadronTradeRemoved>{

    public PacketAmadronTradeRemoved(){}

    public PacketAmadronTradeRemoved(AmadronOfferCustom offer){
        super(offer);
    }

    @Override
    public void handleClientSide(PacketAmadronTradeRemoved message, EntityPlayer player){
        if(AmadronOfferSettings.notifyOfTradeRemoval) player.addChatMessage(new ChatComponentText(I18n.format("message.amadron.playerRemovedTrade", message.getOffer().getVendor(), WidgetAmadronOffer.getStringForObject(message.getOffer().getInput()), WidgetAmadronOffer.getStringForObject(message.getOffer().getOutput()))));
    }

    @Override
    public void handleServerSide(PacketAmadronTradeRemoved message, EntityPlayer player){}
}
