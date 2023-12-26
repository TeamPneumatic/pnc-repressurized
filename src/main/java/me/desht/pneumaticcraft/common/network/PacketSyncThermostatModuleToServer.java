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

import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public class PacketSyncThermostatModuleToServer extends LocationIntPacket {
    private final Direction side;
    private final byte channel;
    private final int threshold;

    public PacketSyncThermostatModuleToServer(ThermostatModule module) {
        super(module.getTube().getBlockPos());

        this.side = module.getDirection();
        this.channel = (byte) module.getColorChannel();
        this.threshold = module.getThreshold();
    }

    PacketSyncThermostatModuleToServer(FriendlyByteBuf buffer) {
        super(buffer);
        side = buffer.readEnum(Direction.class);
        channel = buffer.readByte();
        threshold = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeEnum(side);
        buf.writeByte(channel);
        buf.writeInt(threshold);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (PneumaticCraftUtils.canPlayerReach(player, pos)) {
                PneumaticCraftUtils.getTileEntityAt(player.getLevel(), pos, PressureTubeBlockEntity.class).ifPresent(tube -> {
                    if (tube.getModule(side) instanceof ThermostatModule mr) {
                        mr.setColorChannel(channel);
                        mr.setThreshold(threshold);
                        mr.updateNeighbors();
                        mr.setUpdate(true); // Force recalc
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
