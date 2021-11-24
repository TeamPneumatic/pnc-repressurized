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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to make a tile entity render its area of effect
 */
public class PacketShowArea extends LocationIntPacket {
    private final BlockPos[] area;

    public PacketShowArea(BlockPos pos, BlockPos... area) {
        super(pos);
        this.area = area;
    }

    public PacketShowArea(BlockPos pos, Set<BlockPos> area) {
        this(pos, area.toArray(new BlockPos[0]));
    }

    PacketShowArea(PacketBuffer buffer) {
        super(buffer);
        area = new BlockPos[buffer.readInt()];
        for (int i = 0; i < area.length; i++) {
            area[i] = buffer.readBlockPos();
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeInt(area.length);
        Arrays.stream(area).forEach(buffer::writeBlockPos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AreaRenderManager.getInstance().showArea(area, 0x9000FFFF, ClientUtils.getClientTE(pos)));
        ctx.get().setPacketHandled(true);
    }
}
