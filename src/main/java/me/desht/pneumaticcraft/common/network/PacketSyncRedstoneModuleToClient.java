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
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up the settings of a redstone module
 */
public class PacketSyncRedstoneModuleToClient extends LocationIntPacket {
    private final ModuleRedstone.EnumRedstoneDirection dir;
    private final int outputLevel;
    private final int inputLevel;
    private final int channel;
    private final byte side;

    public PacketSyncRedstoneModuleToClient(ModuleRedstone module) {
        super(module.getTube().getBlockPos());

        this.dir = module.getRedstoneDirection();
        this.outputLevel = module.getRedstoneLevel();
        this.inputLevel = module.getInputLevel();
        this.channel = module.getColorChannel();
        this.side = (byte) module.getDirection().get3DDataValue();
    }

    PacketSyncRedstoneModuleToClient(FriendlyByteBuf buffer) {
        super(buffer);
        dir = ModuleRedstone.EnumRedstoneDirection.values()[buffer.readByte()];
        side = buffer.readByte();
        outputLevel = buffer.readByte();
        inputLevel = buffer.readByte();
        channel = buffer.readByte();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(dir.ordinal());
        buf.writeByte(side);
        buf.writeByte(outputLevel);
        buf.writeByte(inputLevel);
        buf.writeByte(channel);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                PneumaticCraftUtils.getTileEntityAt(ClientUtils.getClientLevel(), pos, TileEntityPressureTube.class).ifPresent(te -> {
                    TubeModule module = te.getModule(Direction.from3DDataValue(side));
                    if (module instanceof ModuleRedstone) {
                        ModuleRedstone mr = (ModuleRedstone) module;
                        mr.setColorChannel(channel);
                        mr.setRedstoneDirection(dir);
                        mr.setOutputLevel(outputLevel);
                        mr.setInputLevel(inputLevel);
                    }
                }));
        ctx.get().setPacketHandled(true);
    }
}
