/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.util.Debugger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

/**
 * @author MineMaarten
 */

public class PacketDebugBlock extends LocationIntPacket<PacketDebugBlock> {

    public PacketDebugBlock() {

    }

    public PacketDebugBlock(BlockPos pos) {

        super(pos);
    }

    @Override
    public void handleClientSide(PacketDebugBlock message, EntityPlayer player) {

        Debugger.indicateBlock(player.world, message.pos);
    }

    @Override
    public void handleServerSide(PacketDebugBlock message, EntityPlayer player) {

    }

}
