package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PacketAmadronTradeAddCustom extends PacketAbstractAmadronTrade<PacketAmadronTradeAddCustom> {

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddCustom() {
    }

    public PacketAmadronTradeAddCustom(AmadronOfferCustom offer) {
        super(offer);
    }

    public PacketAmadronTradeAddCustom(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) {
                // client
                if (AmadronOfferSettings.notifyOfTradeAddition)
                    PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(xlate("message.amadron.playerAddedTrade",
                            getOffer().getVendor(),
                            WidgetAmadronOffer.getStringForObject(getOffer().getOutput()),
                            WidgetAmadronOffer.getStringForObject(getOffer().getInput())), false);
            } else {
                // server
                AmadronOfferCustom offer = getOffer();
                offer.updatePlayerId();
                if (AmadronOfferManager.getInstance().hasOffer(offer.copy().invert())) {
                    player.sendStatusMessage(xlate("message.amadron.duplicateReversedOffer"), false);
                } else if (AmadronOfferManager.getInstance().addStaticOffer(offer)) {
                    if (AmadronOfferSettings.notifyOfTradeAddition) {
                        NetworkHandler.sendToAll(this);
                    }
                    try {
                        AmadronOfferStaticConfig.INSTANCE.writeToFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendStatusMessage(xlate("message.amadron.duplicateOffer"), false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
