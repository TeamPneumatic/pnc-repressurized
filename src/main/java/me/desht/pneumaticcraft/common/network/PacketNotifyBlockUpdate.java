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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 *
 * Sent by server when a block is dropped by shift-wrenching it, rotated by wrenching it, or if a pneumatic BE explodes
 * due to overpressure.
 * This happens server-side (block updates are triggered on the server), but the client needs to know too so that
 * neighbouring cached block shapes (pressure tubes especially, but potentially anything) can be recalculated.
 */
public record PacketNotifyBlockUpdate(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("notify_block_update");

    public static PacketNotifyBlockUpdate fromNetwork(FriendlyByteBuf buffer) {
        return new PacketNotifyBlockUpdate(buffer.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketNotifyBlockUpdate message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Level w = ClientUtils.getClientLevel();
            w.getBlockState(message.pos()).updateNeighbourShapes(w, message.pos(), Block.UPDATE_ALL);
        });
    }
}
