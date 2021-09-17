package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ILeftClickableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when certain items are left-clicked in the air (which is a client-only event)
 */
public class PacketLeftClickEmpty {
    public PacketLeftClickEmpty() {
    }

    @SuppressWarnings("EmptyMethod")
    public PacketLeftClickEmpty(@SuppressWarnings("unused") PacketBuffer buf) {
    }

    @SuppressWarnings("EmptyMethod")
    public void toBytes(@SuppressWarnings("unused") PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                ItemStack stack = ctx.get().getSender().getMainHandItem();
                if (stack.getItem() instanceof ILeftClickableItem) {
                    ((ILeftClickableItem) stack.getItem()).onLeftClickEmpty(ctx.get().getSender());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
