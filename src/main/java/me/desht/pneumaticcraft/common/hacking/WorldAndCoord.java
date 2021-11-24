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

package me.desht.pneumaticcraft.common.hacking;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class WorldAndCoord {
    public final IBlockReader world;
    public final BlockPos pos;

    public WorldAndCoord(IBlockReader world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public Block getBlock() {
        return world.getBlockState(pos).getBlock();
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WorldAndCoord) {
            WorldAndCoord wac = (WorldAndCoord) o;
            return wac.world == world && wac.pos.equals(pos);
        } else {
            return false;
        }
    }
}
