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

import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Arrays;
import java.util.Set;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to make a block entity render its area of effect
 */
public record PacketShowArea(BlockPos pos, BlockPos[] area) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("show_area");

    public static PacketShowArea forPos(BlockPos pos) {
        return new PacketShowArea(pos, new BlockPos[0]);
    }

    public static PacketShowArea forArea(BlockPos pos, Set<BlockPos> area) {
        return new PacketShowArea(pos, area.toArray(new BlockPos[0]));
    }

    public static PacketShowArea fromNetwork(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        var area = new BlockPos[buffer.readInt()];
        for (int i = 0; i < area.length; i++) {
            area[i] = buffer.readBlockPos();
        }
        return new PacketShowArea(pos, area);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(area.length);
        Arrays.stream(area).forEach(buffer::writeBlockPos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketShowArea message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() ->
                AreaRenderManager.getInstance().showArea(message.area(), 0x9000FFFF, ClientUtils.getBlockEntity(message.pos())));
    }
}
