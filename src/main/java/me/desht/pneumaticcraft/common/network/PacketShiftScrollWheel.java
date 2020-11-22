package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when player shift-scrolls the mouse wheel while holding an item implementing the
 * {@link me.desht.pneumaticcraft.common.item.IShiftScrollable} interface.
 */
public class PacketShiftScrollWheel {
    private boolean forward = true;

    public PacketShiftScrollWheel(boolean forward) {
        this.forward = forward;
    }

    public PacketShiftScrollWheel(PacketBuffer buf) {
        this.forward = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(forward);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof IShiftScrollable) {
                    ((IShiftScrollable) stack.getItem()).onShiftScrolled(player, forward);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
