package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
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
                if (PNCConfig.Common.Amadron.notifyOfTradeAddition)
                    PneumaticCraftRepressurized.proxy.getClientPlayer().sendStatusMessage(xlate("message.amadron.playerAddedTrade",
                            getOffer().getVendor(),
                            getOffer().getOutput().toString(),
                            getOffer().getInput().toString()), false);
            } else {
                // server
                AmadronOfferCustom offer = getOffer();
                offer.updatePlayerId();
                if (AmadronOfferManager.getInstance().hasOffer(offer.copy().invert())) {
                    player.sendStatusMessage(xlate("message.amadron.duplicateReversedOffer"), false);
                } else if (AmadronOfferManager.getInstance().addStaticOffer(offer)) {
                    if (PNCConfig.Common.Amadron.notifyOfTradeAddition) {
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
