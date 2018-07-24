package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.PermissionAPI;

import java.io.IOException;

public class PacketAmadronTradeAddStatic extends AbstractPacket<PacketAmadronTradeAddStatic> {
    private AmadronOffer trade;

    @SuppressWarnings("unused")
    public PacketAmadronTradeAddStatic() {
    }

    public PacketAmadronTradeAddStatic(AmadronOffer trade) {
        this.trade = trade;
    }

    @Override
    public void handleClientSide(PacketAmadronTradeAddStatic message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketAmadronTradeAddStatic message, EntityPlayer player) {
        AmadronOffer trade = message.trade;
        if (trade != null && PermissionAPI.hasPermission(player, Names.AMADRON_ADD_STATIC_TRADE)) {
            trade.setAddedBy(player.getName());
            if (AmadronOfferManager.getInstance().addStaticOffer(trade)) {
                try {
                    AmadronOfferStaticConfig.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String in = WidgetAmadronOffer.getStringForObject(trade.getInput());
                String out = WidgetAmadronOffer.getStringForObject(trade.getOutput());
                player.sendStatusMessage(new TextComponentTranslation("message.amadron.addedStaticOffer", in, out), false);
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
