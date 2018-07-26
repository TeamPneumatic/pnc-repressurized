package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.PermissionAPI;

import java.io.IOException;

public class PacketAmadronTradeAddPeriodic extends AbstractPacket<PacketAmadronTradeAddPeriodic> {
    private AmadronOffer trade;

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddPeriodic() {
    }

    public PacketAmadronTradeAddPeriodic(AmadronOffer trade) {
        this.trade = trade;
    }

    @Override
    public void handleClientSide(PacketAmadronTradeAddPeriodic message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketAmadronTradeAddPeriodic message, EntityPlayer player) {
        AmadronOffer trade = message.trade;
        if (trade != null && PermissionAPI.hasPermission(player, Names.AMADRON_ADD_PERIODIC_TRADE)) {
            trade.setAddedBy(player.getName());
            if (AmadronOfferManager.getInstance().addPeriodicOffer(trade)) {
                try {
                    AmadronOfferPeriodicConfig.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String in = WidgetAmadronOffer.getStringForObject(trade.getInput());
                String out = WidgetAmadronOffer.getStringForObject(trade.getOutput());
                player.sendStatusMessage(new TextComponentTranslation("message.amadron.addedPeriodicOffer", in, out), false);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("message.amadron.duplicateOffer"), false);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        trade = AmadronOffer.readFromBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        trade.writeToBuf(buf);
    }

}
