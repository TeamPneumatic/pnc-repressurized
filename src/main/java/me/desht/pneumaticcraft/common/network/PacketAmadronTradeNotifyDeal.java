package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketAmadronTradeNotifyDeal extends PacketAbstractAmadronTrade<PacketAmadronTradeNotifyDeal> {
    private int offerAmount;
    private String buyingPlayer;

    public PacketAmadronTradeNotifyDeal() {
    }

    public PacketAmadronTradeNotifyDeal(AmadronOfferCustom offer, int offerAmount, String buyingPlayer) {
        super(offer);
        this.offerAmount = offerAmount;
        this.buyingPlayer = buyingPlayer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        offerAmount = buf.readInt();
        buyingPlayer = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(offerAmount);
        ByteBufUtils.writeUTF8String(buf, buyingPlayer);
    }

    @Override
    public void handleClientSide(PacketAmadronTradeNotifyDeal message, EntityPlayer player) {
        if (AmadronOfferSettings.notifyOfDealMade)
            player.sendStatusMessage(new TextComponentTranslation("message.amadron.playerBought",
                    message.buyingPlayer,
                    WidgetAmadronOffer.getStringForObject(message.getOffer().getOutput(), message.offerAmount),
                    WidgetAmadronOffer.getStringForObject(message.getOffer().getInput(), message.offerAmount)),
                    false);
    }

    @Override
    public void handleServerSide(PacketAmadronTradeNotifyDeal message, EntityPlayer player) {
    }
}
