/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
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
    private final int lastSlot;
    private final List<Pair<Integer, ItemStack>> filter;

    public PacketSyncSmartChest(TileEntitySmartChest te) {
        super(te.getBlockPos());

        lastSlot = te.getLastSlot();
        filter = te.getFilter();
    }

    PacketSyncSmartChest(FriendlyByteBuf buffer) {
        super(buffer);

        lastSlot = buffer.readVarInt();
        int nFilters = buffer.readVarInt();
        filter = new ArrayList<>();
        for (int i = 0; i < nFilters; i++) {
            int slot = buffer.readVarInt();
            ItemStack stack = buffer.readItem();
            filter.add(Pair.of(slot, stack));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
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
