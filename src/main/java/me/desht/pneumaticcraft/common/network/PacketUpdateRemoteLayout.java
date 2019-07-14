package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.ItemRemote;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item
 */
public class PacketUpdateRemoteLayout {

    private CompoundNBT layout;

    public PacketUpdateRemoteLayout() {
    }

    public PacketUpdateRemoteLayout(CompoundNBT layout) {
        this.layout = layout;
    }

    public PacketUpdateRemoteLayout(PacketBuffer buffer) {
        this.layout = buffer.readCompoundTag();
    }

    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeCompoundTag(layout);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack remote = ctx.get().getSender().getHeldItemMainhand();
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
