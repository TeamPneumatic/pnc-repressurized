package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.common.config.AmadronOfferSettings;
import pneumaticCraft.common.recipes.AmadronOfferCustom;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketAmadronTradeNotifyDeal extends PacketAbstractAmadronTrade<PacketAmadronTradeNotifyDeal>{
    private int offerAmount;
    private String buyingPlayer;

    public PacketAmadronTradeNotifyDeal(){}

    public PacketAmadronTradeNotifyDeal(AmadronOfferCustom offer, int offerAmount, String buyingPlayer){
        super(offer);
        this.offerAmount = offerAmount;
        this.buyingPlayer = buyingPlayer;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        offerAmount = buf.readInt();
        buyingPlayer = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeInt(offerAmount);
        ByteBufUtils.writeUTF8String(buf, buyingPlayer);
    }

    @Override
    public void handleClientSide(PacketAmadronTradeNotifyDeal message, EntityPlayer player){
        if(AmadronOfferSettings.notifyOfDealMade) player.addChatMessage(new ChatComponentText(I18n.format("message.amadron.playerBought", message.buyingPlayer, WidgetAmadronOffer.getStringForObject(message.getOffer().getOutput(), message.offerAmount), WidgetAmadronOffer.getStringForObject(message.getOffer().getInput(), message.offerAmount))));
    }

    @Override
    public void handleServerSide(PacketAmadronTradeNotifyDeal message, EntityPlayer player){}
}
