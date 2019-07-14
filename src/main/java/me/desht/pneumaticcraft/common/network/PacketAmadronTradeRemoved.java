package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
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
            if (AmadronOfferSettings.notifyOfTradeRemoval)
                PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(
                        xlate("message.amadron.playerRemovedTrade",
                                getOffer().getVendor(),
                                WidgetAmadronOffer.getStringForObject(getOffer().getInput()),
                                WidgetAmadronOffer.getStringForObject(getOffer().getOutput())
                        ), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
