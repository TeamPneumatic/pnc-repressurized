package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the offer list changes, or when a player logs in
 */
public class PacketSyncAmadronOffers {
    private final Collection<AmadronOffer> activeOffers;

    public PacketSyncAmadronOffers() {
        this.activeOffers = AmadronOfferManager.getInstance().getActiveOffers();
    }

    public PacketSyncAmadronOffers(PacketBuffer buf) {
        this.activeOffers = readOffers(buf);
    }

    private Collection<AmadronOffer> readOffers(PacketBuffer buf) {
        int offerCount = buf.readVarInt();
        List<AmadronOffer> offers = new ArrayList<>();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                offers.add(AmadronPlayerOffer.loadFromBuf(buf));
            } else {
                offers.add(AmadronOffer.readFromBuf(buf));
            }
        }
        return offers;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(activeOffers.size());
        for (AmadronOffer offer : activeOffers) {
            buf.writeBoolean(offer instanceof AmadronPlayerOffer);
            buf.writeResourceLocation(offer.getId());
            offer.write(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AmadronOfferManager.getInstance().syncOffers(activeOffers));
        ctx.get().setPacketHandled(true);
    }

}
