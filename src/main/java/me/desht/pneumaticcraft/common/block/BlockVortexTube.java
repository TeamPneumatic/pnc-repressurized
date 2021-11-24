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

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockVortexTube extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {

    private static final VoxelShape[] SHAPES = new VoxelShape[] {  // DUNSWE order
            Block.box(0,0, 0, 15, 15, 15),
            Block.box(0,1, 1, 15, 16, 16),
            Block.box(1,0, 0, 16, 15, 15),
            Block.box(0,0, 1, 15, 15, 16),
            Block.box(0,0, 0, 15, 15, 15),
            Block.box(1,0, 1, 16, 15, 16),
    };

    public BlockVortexTube() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
            .setValue(DOWN, false)
            .setValue(UP, false)
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTION_PROPERTIES);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVortexTube.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[getRotation(state).get3DDataValue()];
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }
}
