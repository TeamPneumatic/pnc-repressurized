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
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockUVLightBox extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    private static final VoxelShape SHAPE_EW = Block.box(1, 0, 4.5, 15, 7, 11.5);
    private static final VoxelShape SHAPE_NS = Block.box(4.5, 0, 1, 11.5, 7, 15);

    public BlockUVLightBox() {
        super(ModBlocks.defaultProps().lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        registerDefaultState(getStateDefinition().any().setValue(LOADED, false).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOADED, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return getRotation(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUVLightBox.class;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

//    @Override
//    public int getLightValue(BlockState state) {
//        return state.get(LIT) ? 15 : 0;
//    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            return state.hasProperty(BlockUVLightBox.LIT) && state.getValue(BlockUVLightBox.LIT) ? 0xFF4000FF : 0xFFAFAFE4;
        }
        return 0xFFAFAFE4;
    }
}
