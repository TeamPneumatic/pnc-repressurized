/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.util.Debugger;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author MineMaarten
 *
 * Received on: CLIENT
 */
public class PacketDebugBlock extends LocationIntPacket {
    public PacketDebugBlock() {
    }

    public PacketDebugBlock(BlockPos pos) {
        super(pos);
    }

    PacketDebugBlock(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Debugger.indicateBlock(PneumaticCraftRepressurized.proxy.getClientWorld(), pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
