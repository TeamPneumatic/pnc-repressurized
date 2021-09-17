package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;

public abstract class PacketAbstractAmadronTrade {
    private final AmadronPlayerOffer offer;

    public PacketAbstractAmadronTrade(AmadronPlayerOffer offer) {
        this.offer = offer;
    }

    public PacketAbstractAmadronTrade(PacketBuffer buffer) {
        offer = AmadronPlayerOffer.playerOfferFromBuf(buffer.readResourceLocation(), buffer);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(offer.getId());
        offer.write(buf);
    }

    public AmadronPlayerOffer getOffer() {
        return offer;
    }

}
