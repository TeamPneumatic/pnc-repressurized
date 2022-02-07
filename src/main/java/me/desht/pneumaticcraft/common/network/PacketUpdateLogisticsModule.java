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

import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when the status or colour of a logistics module is updated
 */
public class PacketUpdateLogisticsModule extends LocationIntPacket {
    private final int side;
    private final int colorIndex;
    private final int status;

    public PacketUpdateLogisticsModule(ModuleLogistics logisticsModule, int action) {
        super(logisticsModule.getTube().getBlockPos());
        side = logisticsModule.getDirection().ordinal();
        colorIndex = logisticsModule.getColorChannel();
        if (action > 0) {
            status = 1 + action;
        } else {
            status = logisticsModule.hasPower() ? 1 : 0;
        }
    }

    public PacketUpdateLogisticsModule(FriendlyByteBuf buffer) {
        super(buffer);
        side = buffer.readByte();
        colorIndex = buffer.readByte();
        status = buffer.readByte();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(side);
        buf.writeByte(colorIndex);
        buf.writeByte(status);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PacketUtil.getTE(ctx.get().getSender(), pos, TileEntityPressureTube.class).ifPresent(te -> {
            TubeModule module = te.getModule(Direction.from3DDataValue(side));
            if (module instanceof ModuleLogistics) {
                ((ModuleLogistics) module).onUpdatePacket(status, colorIndex);
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
