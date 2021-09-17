package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when an order is purchased by someone, to update remaining stock levels as seen on clients.
 */
public class PacketAmadronStockUpdate {
    private final ResourceLocation id;
    private final int stock;

    public PacketAmadronStockUpdate(ResourceLocation id, int stock) {
        this.id = id;
        this.stock = stock;
    }

    public PacketAmadronStockUpdate(PacketBuffer buffer) {
        this.id = buffer.readResourceLocation();
        this.stock = buffer.readVarInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(id);
        buf.writeVarInt(stock);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                AmadronOfferManager.getInstance().updateStock(id, stock);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
