package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PacketAmadronTradeRemoved extends PacketAbstractAmadronTrade<PacketAmadronTradeRemoved> {

    public PacketAmadronTradeRemoved() {
    }

    public PacketAmadronTradeRemoved(AmadronOfferCustom offer) {
        super(offer);
    }

    public PacketAmadronTradeRemoved(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (PNCConfig.Common.Amadron.notifyOfTradeRemoval)
                PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(
                        xlate("message.amadron.playerRemovedTrade",
                                getOffer().getVendor(),
                                getOffer().getInput().toString(),
                                getOffer().getOutput().toString()
                        ), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
