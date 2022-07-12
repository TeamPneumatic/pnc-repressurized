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

package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public class BlockPressureChamberWall extends BlockPressureChamberWallBase {
    public enum WallState implements IStringSerializable {
        NONE, CENTER, XEDGE, ZEDGE, YEDGE, XMIN_YMIN_ZMIN, XMIN_YMIN_ZMAX, XMIN_YMAX_ZMIN, XMIN_YMAX_ZMAX;

        @Override
        public String getSerializedName() {
            return toString().toLowerCase(Locale.ROOT);
        }
    }

    public BlockPressureChamberWall() {
        super(IBlockPressureChamber.pressureChamberBlockProps());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WALL_STATE);
    }
}
