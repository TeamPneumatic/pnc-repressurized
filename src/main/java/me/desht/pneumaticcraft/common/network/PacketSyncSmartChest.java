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
import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity.FilterSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * Sent by server when smart chest GUI is being opened to sync filter settings to client
 * Sent by client GUI to sync changed filter settings to server
 */
public record PacketSyncSmartChest(BlockPos pos, int lastSlot, List<FilterSlot> filter) implements CustomPacketPayload {
    public static final Type<PacketSyncSmartChest> TYPE = new Type<>(RL("sync_smart_chest"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSmartChest> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSyncSmartChest::pos,
            ByteBufCodecs.VAR_INT, PacketSyncSmartChest::lastSlot,
            FilterSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketSyncSmartChest::filter,
            PacketSyncSmartChest::new
    );

    public static PacketSyncSmartChest forBlockEntity(SmartChestBlockEntity te) {
        return new PacketSyncSmartChest(te.getBlockPos(), te.getLastSlot(), te.getFilter());
    }

    @Override
    public Type<PacketSyncSmartChest> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSmartChest message, IPayloadContext ctx) {
        PacketUtil.getBlockEntity(ctx.player(), message.pos(), SmartChestBlockEntity.class).ifPresent(te -> {
            te.setLastSlot(message.lastSlot());
            te.setFilter(message.filter());
        });
    }

}
