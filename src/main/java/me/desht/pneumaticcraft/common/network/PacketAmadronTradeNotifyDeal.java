package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Received on: CLIENT
 */
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

    public PacketAmadronTradeNotifyDeal(PacketBuffer buffer) {
        super(buffer);
        offerAmount = buffer.readInt();
        buyingPlayer = PacketUtil.readUTF8String(buffer);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(offerAmount);
        PacketUtil.writeUTF8String(buf, buyingPlayer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (AmadronOfferSettings.notifyOfDealMade)
                PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(
                        xlate("message.amadron.playerBought",
                                buyingPlayer,
                                WidgetAmadronOffer.getStringForObject(getOffer().getOutput(), offerAmount),
                                WidgetAmadronOffer.getStringForObject(getOffer().getInput(), offerAmount)
                        ), false
                );
        });
        ctx.get().setPacketHandled(true);
    }
}
