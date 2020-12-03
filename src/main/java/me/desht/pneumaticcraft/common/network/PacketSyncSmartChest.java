package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * Sent by server when smart chest GUI is being opened to sync filter settings to client
 * Sent by client GUI to sync changed filter settings to server
 */
public class PacketSyncSmartChest extends LocationIntPacket {
    private int lastSlot;
    private List<Pair<Integer, ItemStack>> filter;

    public PacketSyncSmartChest() {
    }

    public PacketSyncSmartChest(TileEntitySmartChest te) {
        super(te.getPos());

        lastSlot = te.getLastSlot();
        filter = te.getFilter();
    }

    PacketSyncSmartChest(PacketBuffer buffer) {
        super(buffer);

        lastSlot = buffer.readVarInt();
        int nFilters = buffer.readVarInt();
        filter = new ArrayList<>();
        for (int i = 0; i < nFilters; i++) {
            int slot = buffer.readVarInt();
            ItemStack stack = buffer.readItemStack();
            filter.add(Pair.of(slot, stack));
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);

        buf.writeVarInt(lastSlot);
        buf.writeVarInt(filter.size());
        for (Pair<Integer,ItemStack> p: filter) {
            buf.writeVarInt(p.getLeft());
            buf.writeItemStack(p.getRight(), true);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PacketUtil.getTE(ctx.get().getSender(), pos, TileEntitySmartChest.class).ifPresent(te -> {
                te.setLastSlot(lastSlot);
                te.setFilter(filter);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
