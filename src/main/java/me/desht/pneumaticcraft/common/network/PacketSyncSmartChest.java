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

import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * Sent by server when smart chest GUI is being opened to sync filter settings to client
 * Sent by client GUI to sync changed filter settings to server
 */
public record PacketSyncSmartChest(BlockPos pos, int lastSlot, List<Pair<Integer, ItemStack>> filter) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_smart_chest");

    public static PacketSyncSmartChest forBlockEntity(SmartChestBlockEntity te) {
        return new PacketSyncSmartChest(te.getBlockPos(), te.getLastSlot(), te.getFilter());
    }

    public static PacketSyncSmartChest fromNetwork(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int lastSlot = buffer.readVarInt();
        var filter = buffer.readList(buf -> Pair.of(buf.readVarInt(), buf.readItem()));

        return new PacketSyncSmartChest(pos, lastSlot, filter);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(lastSlot);
        buf.writeCollection(filter, (b, pair) -> {
            b.writeVarInt(pair.getLeft());
            b.writeItem(pair.getRight());
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncSmartChest message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            PacketUtil.getBlockEntity(player, message.pos(), SmartChestBlockEntity.class).ifPresent(te -> {
                te.setLastSlot(message.lastSlot());
                te.setFilter(message.filter());
            });
        }));
    }
}
