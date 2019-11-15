package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import net.minecraft.network.PacketBuffer;

public abstract class PacketAbstractAmadronTrade<REQ extends PacketAbstractAmadronTrade<REQ>> {
    private AmadronOfferCustom offer;

    public PacketAbstractAmadronTrade() {
    }

    public PacketAbstractAmadronTrade(AmadronOfferCustom offer) {
        this.offer = offer;
    }

    public PacketAbstractAmadronTrade(PacketBuffer buffer) {
        offer = AmadronOfferCustom.loadFromBuf(buffer);
    }

    public void toBytes(PacketBuffer buf) {
        offer.writeToBuf(buf);
    }

    public AmadronOfferCustom getOffer() {
        return offer;
    }

}
