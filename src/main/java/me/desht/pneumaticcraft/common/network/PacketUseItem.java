package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to consume item(s) from the player's inventory. If client is lying about the item, keeeeeeel them!
 */
public class PacketUseItem {

    private ItemStack stack;

    public PacketUseItem() {
    }

    public PacketUseItem(ItemStack stack) {
        this.stack = stack;
    }

    public PacketUseItem(PacketBuffer buffer) {
        this.stack = buffer.readItemStack();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeItemStack(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!PneumaticCraftUtils.consumeInventoryItem(ctx.get().getSender().inventory, stack)) {
                // lying client!
                ctx.get().getSender().attackEntityFrom(DamageSource.OUT_OF_WORLD, 2000);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
