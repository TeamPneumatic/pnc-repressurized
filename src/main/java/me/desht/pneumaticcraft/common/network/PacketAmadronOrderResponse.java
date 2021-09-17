package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.gui.GuiAmadron;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to confirm updated amount when player adjusts an offer with PacketAmadronOrderUpdate
 * (This needs a round trip rather than just updating client-side, since server needs to validate and cap
 *  requested shopping amounts)
 */
public class PacketAmadronOrderResponse {
    private final ResourceLocation offerId;
    private final int amount;

    public PacketAmadronOrderResponse(ResourceLocation offerId, int amount) {
        this.offerId = offerId;
        this.amount = amount;
    }

    public PacketAmadronOrderResponse(PacketBuffer buf) {
        this.offerId = buf.readResourceLocation();
        this.amount = buf.readVarInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(offerId);
        buf.writeVarInt(amount);
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (player.containerMenu instanceof ContainerAmadron) {
                ((ContainerAmadron) player.containerMenu).updateBasket(offerId, amount);
                GuiAmadron.basketUpdated();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
