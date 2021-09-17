package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the offer list changes (due to a shuffle or reload),
 * or when a player logs in
 */
public class PacketSyncAmadronOffers {
    private final Collection<AmadronRecipe> activeOffers;
    private final boolean notifyPlayer;

    public PacketSyncAmadronOffers(boolean notifyPlayer) {
        this.notifyPlayer = notifyPlayer;
        this.activeOffers = AmadronOfferManager.getInstance().getActiveOffers();
    }

    public PacketSyncAmadronOffers(PacketBuffer buf) {
        this.notifyPlayer = buf.readBoolean();
        this.activeOffers = new ArrayList<>();
        int offerCount = buf.readVarInt();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                activeOffers.add(AmadronPlayerOffer.playerOfferFromBuf(buf.readResourceLocation(), buf));
            } else {
                activeOffers.add(AmadronOffer.offerFromBuf(buf.readResourceLocation(), buf));
            }
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(notifyPlayer);
        buf.writeVarInt(activeOffers.size());
        for (AmadronRecipe offer : activeOffers) {
            buf.writeBoolean(offer instanceof AmadronPlayerOffer);
            buf.writeResourceLocation(offer.getId());
            offer.write(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AmadronOfferManager.getInstance().syncOffers(activeOffers, notifyPlayer));
        ctx.get().setPacketHandled(true);
    }

}
