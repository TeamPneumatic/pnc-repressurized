package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when player shift-scrolls the mouse wheel while holding an item implementing the
 * {@link me.desht.pneumaticcraft.common.item.IShiftScrollable} interface.
 */
public class PacketShiftScrollWheel {
    private final boolean forward;
    private final boolean mainHand;

    public PacketShiftScrollWheel(boolean forward, Hand mainHand) {
        this.forward = forward;
        this.mainHand = mainHand == Hand.MAIN_HAND;
    }

    public PacketShiftScrollWheel(PacketBuffer buf) {
        this.forward = buf.readBoolean();
        this.mainHand = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(forward);
        buf.writeBoolean(mainHand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof IShiftScrollable) {
                    ((IShiftScrollable) stack.getItem()).onShiftScrolled(player, forward, mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
