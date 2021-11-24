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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent periodically from server to sync pressure level:
 * - For pressure tubes with an attached pressure gauge module
 * - For air grate modules, when the pressure changes enough to modify the range
 * - For machine air handlers which are currently leaking
 */
public class PacketUpdatePressureBlock extends LocationIntPacket {
    private static final byte NO_DIRECTION = 127;

    private final Direction leakDir;
    private final Direction handlerDir;
    private final int currentAir;

    public PacketUpdatePressureBlock(TileEntity te, Direction handlerDir, Direction leakDir, int currentAir) {
        super(te.getBlockPos());

        this.handlerDir = handlerDir;
        this.leakDir = leakDir;
        this.currentAir = currentAir;
    }

    public PacketUpdatePressureBlock(PacketBuffer buffer) {
        super(buffer);
        this.currentAir = buffer.readInt();
        byte idx = buffer.readByte();
        this.handlerDir = idx >= 0 && idx < 6 ? Direction.from3DDataValue(idx) : null;
        idx = buffer.readByte();
        this.leakDir = idx >= 0 && idx < 6 ? Direction.from3DDataValue(idx) : null;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeInt(currentAir);
        buf.writeByte(handlerDir == null ? NO_DIRECTION : handlerDir.get3DDataValue());
        buf.writeByte(leakDir == null ? NO_DIRECTION : leakDir.get3DDataValue());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ClientUtils.getClientTE(pos);
            if (te != null) {
                te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, handlerDir).ifPresent(handler -> {
                    handler.setSideLeaking(leakDir);
                    handler.addAir(currentAir - handler.getAir());
                    if (handlerDir != null && te instanceof TileEntityPneumaticBase) {
                        ((TileEntityPneumaticBase) te).initializeHullAirHandlerClient(handlerDir, handler);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
