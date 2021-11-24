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

import me.desht.pneumaticcraft.common.block.tubes.INetworkedModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public class PacketTubeModuleColor extends LocationIntPacket {
    private final int ourColor;
    private final Direction side;

    public PacketTubeModuleColor(TubeModule module) {
        super(module.getTube().getBlockPos());

        this.ourColor = ((INetworkedModule) module).getColorChannel();
        this.side = module.getDirection();
    }

    PacketTubeModuleColor(PacketBuffer buffer) {
        super(buffer);

        this.ourColor = buffer.readByte();
        this.side = Direction.from3DDataValue(buffer.readByte());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);

        buf.writeByte(ourColor);
        buf.writeByte(side.get3DDataValue());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                PneumaticCraftUtils.getTileEntityAt(player.level, pos, TileEntityPressureTube.class).ifPresent(te -> {
                    TubeModule module = te.getModule(side);
                    if (module instanceof INetworkedModule) {
                        ((INetworkedModule) module).setColorChannel(ourColor);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
