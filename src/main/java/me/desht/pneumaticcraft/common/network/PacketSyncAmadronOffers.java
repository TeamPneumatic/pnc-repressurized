package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up current Amadron offer list when the tablet is opened
 */
public class PacketSyncAmadronOffers {
    private Collection<AmadronOffer> staticOffers = new ArrayList<>();
    private Collection<AmadronOffer> selectedPeriodicOffers = new ArrayList<>();
//    private boolean mayAddPeriodic;
//    private boolean mayAddStatic;

    public PacketSyncAmadronOffers() {
        this.staticOffers = AmadronOfferManager.getInstance().getStaticOffers();
        this.selectedPeriodicOffers = AmadronOfferManager.getInstance().getSelectedPeriodicOffers();
    }

//    public PacketSyncAmadronOffers(PlayerEntity playerIn) {
//
////        this.mayAddPeriodic = playerIn != null && PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_PERIODIC_TRADE);
////        this.mayAddStatic = playerIn != null && PermissionAPI.hasPermission(playerIn, Names.AMADRON_ADD_STATIC_TRADE);
//    }

    public PacketSyncAmadronOffers(PacketBuffer buf) {
        this.staticOffers = readOffers(buf);
        this.selectedPeriodicOffers = readOffers(buf);
//        this.mayAddPeriodic = buf.readBoolean();
//        this.mayAddStatic = buf.readBoolean();
    }

    private Collection<AmadronOffer> readOffers(PacketBuffer buf) {
        int offerCount = buf.readInt();
        List<AmadronOffer> offers = new ArrayList<>();
        for (int i = 0; i < offerCount; i++) {
            if (buf.readBoolean()) {
                offers.add(AmadronOfferCustom.loadFromBuf(buf));
            } else {
                offers.add(AmadronOffer.readFromBuf(buf));
            }
        }
        return offers;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(staticOffers.size());
        for (AmadronOffer offer : staticOffers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            offer.writeToBuf(buf);
        }
        buf.writeInt(selectedPeriodicOffers.size());
        for (AmadronOffer offer : selectedPeriodicOffers) {
            buf.writeBoolean(offer instanceof AmadronOfferCustom);
            offer.writeToBuf(buf);
        }
//        buf.writeBoolean(mayAddPeriodic);
//        buf.writeBoolean(mayAddStatic);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AmadronOfferManager.getInstance().syncOffers(staticOffers, selectedPeriodicOffers);
//            ContainerAmadron.mayAddPeriodicOffers = mayAddPeriodic;
//            ContainerAmadron.mayAddStaticOffers = mayAddStatic;
        });
        ctx.get().setPacketHandled(true);
    }

}
