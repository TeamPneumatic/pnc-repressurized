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

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberWall;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

public class BlockPressureChamberWall extends BlockPressureChamberWallBase {
    public enum EnumWallState implements StringRepresentable {
        NONE, CENTER, XEDGE, ZEDGE, YEDGE, XMIN_YMIN_ZMIN, XMIN_YMIN_ZMAX, XMIN_YMAX_ZMIN, XMIN_YMAX_ZMAX;

        @Override
        public String getSerializedName() {
            return toString().toLowerCase(Locale.ROOT);
        }
    }

    public BlockPressureChamberWall() {
        super(IBlockPressureChamber.pressureChamberBlockProps());
    }

    private static final EnumProperty<EnumWallState> WALL_STATE = EnumProperty.create("wall_state", EnumWallState.class);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WALL_STATE);
    }

    public BlockState updateState(BlockState state, BlockGetter world, BlockPos pos) {
        return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberWall.class).map(wall -> {
            EnumWallState wallState = EnumWallState.NONE;
            TileEntityPressureChamberValve core = wall.getCore();
            if (core != null) {
                boolean xMin = pos.getX() == core.multiBlockX;
                boolean yMin = pos.getY() == core.multiBlockY;
                boolean zMin = pos.getZ() == core.multiBlockZ;
                boolean xMax = pos.getX() == core.multiBlockX + core.multiBlockSize - 1;
                boolean yMax = pos.getY() == core.multiBlockY + core.multiBlockSize - 1;
                boolean zMax = pos.getZ() == core.multiBlockZ + core.multiBlockSize - 1;

                //Corners
                if (xMin && yMin && zMin || xMax && yMax && zMax) {
                    wallState = EnumWallState.XMIN_YMIN_ZMIN;
                } else if (xMin && yMin && zMax || xMax && yMax && zMin) {
                    wallState = EnumWallState.XMIN_YMIN_ZMAX;
                } else if (xMin && yMax && zMax || xMax && yMin && zMin) {
                    wallState = EnumWallState.XMIN_YMAX_ZMAX;
                } else if (xMin && yMax && zMin || xMax && yMin && zMax) {
                    wallState = EnumWallState.XMIN_YMAX_ZMIN;
                }
                //Edges
                else if (yMin && xMin || yMax && xMax || yMin && xMax || yMax && xMin) {
                    wallState = EnumWallState.XEDGE;
                } else if (yMin && zMin || yMax && zMax || yMin && zMax || yMax && zMin) {
                    wallState = EnumWallState.ZEDGE;
                } else if (!yMin && !yMax) {
                    if (xMin && zMin || xMax && zMax || xMin && zMax || xMax && zMin) {
                        wallState = EnumWallState.YEDGE;
                    } else {
                        wallState = EnumWallState.CENTER;
                    }
                } else {
                    wallState = EnumWallState.CENTER;
                }
            }
            return state.setValue(WALL_STATE, wallState);
        }).orElse(state);
    }
}
