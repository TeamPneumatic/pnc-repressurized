package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.config.aux.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when "Add Periodic" button clicked in Amadron GUI
 */
public class PacketAmadronTradeAddPeriodic {
    private AmadronOffer trade;

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddPeriodic() {
    }

    public PacketAmadronTradeAddPeriodic(AmadronOffer trade) {
        this.trade = trade;
    }

    public PacketAmadronTradeAddPeriodic(PacketBuffer buffer) {
        this.trade = AmadronOffer.readFromBuf(buffer);
    }

    public void toBytes(PacketBuffer buf) {
        trade.writeToBuf(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (trade != null && PermissionAPI.hasPermission(player, Names.AMADRON_ADD_PERIODIC_TRADE)) {
                trade.setAddedBy(player.getName().getFormattedText());
                if (AmadronOfferManager.getInstance().addPeriodicOffer(trade)) {
                    try {
                        AmadronOfferPeriodicConfig.INSTANCE.writeToFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String in = trade.getInput().toString();
                    String out = trade.getOutput().toString();
                    player.sendStatusMessage(new TranslationTextComponent("message.amadron.addedPeriodicOffer", in, out), false);
                } else {
                    player.sendStatusMessage(new TranslationTextComponent("message.amadron.duplicateOffer"), false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
