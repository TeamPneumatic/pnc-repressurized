package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemRemote;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item from the Remote GUI
 */
public class PacketUpdateRemoteLayout {
    private final CompoundNBT layout;
    private final Hand hand;

    public PacketUpdateRemoteLayout(CompoundNBT layout, Hand hand) {
        this.layout = layout;
        this.hand = hand;
    }

    public PacketUpdateRemoteLayout(PacketBuffer buffer) {
        this.layout = buffer.readCompoundTag();
        this.hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeCompoundTag(layout);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack remote = ctx.get().getSender().getHeldItem(hand);
            if (remote.getItem() instanceof ItemRemote) {
                CompoundNBT tag = remote.getTag();
                if (tag == null) {
                    tag = new CompoundNBT();
                    remote.setTag(tag);
                }
                tag.put("actionWidgets", layout.getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
