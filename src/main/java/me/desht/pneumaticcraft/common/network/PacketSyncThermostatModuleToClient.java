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
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up the settings of a redstone module
 */
public class PacketSyncThermostatModuleToClient extends LocationIntPacket {
    private final Direction side;
    private final int channel;
    private final int level;
    private final int temperature;

    public PacketSyncThermostatModuleToClient(ThermostatModule module) {
        super(module.getTube().getBlockPos());

        this.channel = module.getColorChannel();
        this.side = module.getDirection();
        this.level = module.getInputLevel();
        this.temperature = module.getTemperature();
    }

    PacketSyncThermostatModuleToClient(FriendlyByteBuf buffer) {
        super(buffer);
        channel = buffer.readByte();
        side = buffer.readEnum(Direction.class);
        level = buffer.readInt();
        temperature = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(channel);
        buf.writeEnum(side);
        buf.writeInt(level);
        buf.writeInt(temperature);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                PneumaticCraftUtils.getTileEntityAt(ClientUtils.getClientLevel(), pos, PressureTubeBlockEntity.class).ifPresent(te -> {
                    if (te.getModule(side) instanceof ThermostatModule mr) {
                        mr.setColorChannel(channel);
                        mr.setInputLevel(level);
                        mr.setTemperature(temperature);
                    }
                }));
        ctx.get().setPacketHandled(true);
    }
}
