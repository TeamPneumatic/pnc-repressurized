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

import me.desht.pneumaticcraft.common.block.entity.GasLiftBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class GasLiftBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 4, 0, 5, 14, 5),
            Block.box(0, 4, 11, 5, 14, 16),
            Block.box(11, 4, 11, 16, 14, 16),
            Block.box(11, 4, 0, 16, 14, 5),
            Block.box(2, 14, 2, 14, 15, 14),
            Block.box(2, 2, 2, 14, 4, 14),
            Block.box(11, 4, 5, 16, 14, 11),
            Block.box(0, 4, 5, 5, 14, 11),
            Block.box(5, 4, 0, 11, 14, 16),
            Block.box(0, 14, 0, 16, 16, 2),
            Block.box(0, 14, 14, 16, 16, 16),
            Block.box(0, 14, 2, 2, 16, 14),
            Block.box(14, 14, 2, 16, 16, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public GasLiftBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(UP, false)
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
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(UP, NORTH, SOUTH, WEST, EAST);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GasLiftBlockEntity(pPos, pState);
    }
}
