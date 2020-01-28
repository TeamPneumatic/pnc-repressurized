package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;

public abstract class PacketAbstractAmadronTrade {
    private AmadronPlayerOffer offer;

    public PacketAbstractAmadronTrade() {
    }

    public PacketAbstractAmadronTrade(AmadronPlayerOffer offer) {
        this.offer = offer;
    }

    public PacketAbstractAmadronTrade(PacketBuffer buffer) {
        offer = AmadronPlayerOffer.loadFromBuf(buffer);
    }

    public void toBytes(PacketBuffer buf) {
        offer.writeToBuf(buf);
    }

    public AmadronPlayerOffer getOffer() {
        return offer;
    }

}
