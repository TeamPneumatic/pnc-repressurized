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

import me.desht.pneumaticcraft.common.block.entity.SpawnerExtractorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SpawnerExtractorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
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
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public SpawnerExtractorBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState below = worldIn.getBlockState(pos.below());
        return below.getBlock() instanceof SpawnerBlock || below.getBlock() instanceof EmptySpawnerBlock;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        world.getBlockEntity(pos, ModBlockEntityTypes.SPAWNER_EXTRACTOR.get())
                .ifPresent(SpawnerExtractorBlockEntity::updateMode);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            worldIn.getBlockEntity(currentPos, ModBlockEntityTypes.SPAWNER_EXTRACTOR.get())
                    .ifPresent(SpawnerExtractorBlockEntity::updateMode);
            return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SpawnerExtractorBlockEntity(pPos, pState);
    }
}
