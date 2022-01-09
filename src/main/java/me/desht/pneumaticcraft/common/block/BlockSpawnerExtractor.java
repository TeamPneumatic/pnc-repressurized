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

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class BlockSpawnerExtractor extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(10, 12, 2, 11, 31, 3),
            Block.box(-0.5, -2.5, 13.5, 16.5, 0.5, 16.5),
            Block.box(-0.5, -2.5, -0.5, 16.5, 0.5, 2.5),
            Block.box(-0.5, -2.5, 2.5, 2.5, 0.5, 13.5),
            Block.box(13.5, -2.5, 2.5, 16.5, 0.5, 13.5),
            Block.box(-0.5, -13.5, 15.5, 0.5, -2.5, 16.5),
            Block.box(-0.5, -16, 13.5, 16.5, -13, 16.5),
            Block.box(13.5, -16, 2.5, 16.5, -13, 13.5),
            Block.box(-0.5, -16, 2.5, 2.5, -13, 13.5),
            Block.box(-0.5, -16, -0.5, 16.5, -13, 2.5),
            Block.box(-0.5, -13.5, 13.5, 0.5, -2.5, 14.5),
            Block.box(1.5, -13.5, 15.5, 2.5, -2.5, 16.5),
            Block.box(15.5, -13.5, 13.5, 16.5, -2.5, 14.5),
            Block.box(13.5, -13.5, 15.5, 14.5, -2.5, 16.5),
            Block.box(15.5, -13.5, 15.5, 16.5, -2.5, 16.5),
            Block.box(-0.5, -13.5, 1.5, 0.5, -2.5, 2.5),
            Block.box(-0.5, -13.5, -0.5, 0.5, -2.5, 0.5),
            Block.box(1.5, -13.5, -0.5, 2.5, -2.5, 0.5),
            Block.box(15.5, -13.5, 1.5, 16.5, -2.5, 2.5),
            Block.box(13.5, -13.5, -0.5, 14.5, -2.5, 0.5),
            Block.box(15.5, -13.5, -0.5, 16.5, -2.5, 0.5),
            Block.box(0, 0, 0, 1, 13, 1),
            Block.box(0, 12, 1, 1, 13, 15),
            Block.box(0, 0, 15, 1, 13, 16),
            Block.box(1, 12, 0, 15, 13, 1),
            Block.box(15, 0, 0, 16, 13, 1),
            Block.box(15, 12, 1, 16, 13, 15),
            Block.box(15, 0, 15, 16, 13, 16),
            Block.box(1, 12, 15, 15, 13, 16),
            Block.box(4, 12, 4, 12, 13, 12),
            Block.box(5, 13, 5, 11, 29, 11),
            Block.box(4, 29, 4, 12, 32, 12),
            Block.box(10, 30, 3, 11, 31, 4),
            Block.box(9.5, 29.5, 3.75, 11.5, 31.5, 4.75),
            Block.box(9.5, 11.25, 1.5, 11.5, 12.25, 3.5),
            Block.box(0.5, 0, 0.5, 15.5, 12, 15.5)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    public BlockSpawnerExtractor() {
        super(ModBlocks.defaultProps());

        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(NORTH, SOUTH, WEST, EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySpawnerExtractor.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState below = worldIn.getBlockState(pos.below());
        return below.getBlock() instanceof SpawnerBlock || below.getBlock() instanceof BlockEmptySpawner;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntitySpawnerExtractor.class)
                .ifPresent(TileEntitySpawnerExtractor::updateMode);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            PneumaticCraftUtils.getTileEntityAt(worldIn, currentPos, TileEntitySpawnerExtractor.class)
                    .ifPresent(TileEntitySpawnerExtractor::updateMode);
            return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
    }
}
