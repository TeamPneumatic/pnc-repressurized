package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
    private List<Pair<Integer, Item>> filter;

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
            Item item = Item.getItemById(buffer.readVarInt());
            filter.add(Pair.of(slot, item));
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);

        buf.writeVarInt(lastSlot);
        buf.writeVarInt(filter.size());
        for (Pair<Integer,Item> p: filter) {
            buf.writeVarInt(p.getLeft());
            buf.writeVarInt(Item.getIdFromItem(p.getRight().getItem()));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ctx.get().getSender() == null ? ClientUtils.getClientWorld() : ctx.get().getSender().getServerWorld();
            process(w);
        });
        ctx.get().setPacketHandled(true);
    }

    private void process(World w) {
        TileEntity te = w.getTileEntity(pos);
        if (te instanceof TileEntitySmartChest) {
            TileEntitySmartChest teSC = (TileEntitySmartChest) te;
            teSC.setLastSlot(lastSlot);
            teSC.setFilter(filter);
        }
    }
}
