package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PacketAmadronTradeRemoved extends PacketAbstractAmadronTrade {
    public PacketAmadronTradeRemoved(AmadronPlayerOffer offer) {
        super(offer);
    }

    public PacketAmadronTradeRemoved(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ConfigHelper.common().amadron.notifyOfTradeRemoval.get())
                ClientUtils.getClientPlayer().displayClientMessage(
                        xlate("pneumaticcraft.message.amadron.playerRemovedTrade",
                                getOffer().getVendorName(),
                                getOffer().getOutput().toString(),
                                getOffer().getInput().toString()
                        ), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
