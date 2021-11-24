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
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockSpawnerExtractor extends BlockPneumaticCraft {
    private static final VoxelShape BASE0 = box(-1, -1, -1, 17, 0, 17);
    private static final VoxelShape BASE1 = box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape BASE2 = box(2, 2, 2, 14, 4, 14);
    private static final VoxelShape BASE3 = box(6, 4, 7, 10, 12, 9);
    private static final VoxelShape BASE4 = box(7, 4, 6, 9, 12, 10);
    private static final VoxelShape SHAPE = VoxelShapeUtils.combine(IBooleanFunction.OR, BASE0, BASE1, BASE2, BASE3, BASE4);

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
