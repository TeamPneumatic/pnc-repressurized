package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
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
        buyingPlayer = buffer.readString();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeInt(offerAmount);
        buf.writeString(buyingPlayer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (PNCConfig.Common.Amadron.notifyOfDealMade)
                PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(
                        xlate("message.amadron.playerBought",
                                buyingPlayer,
                                getOffer().getOutput().toString(),
                                getOffer().getInput().toString()
                        ), false
                );
        });
        ctx.get().setPacketHandled(true);
    }
}
