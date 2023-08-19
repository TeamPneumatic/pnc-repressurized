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

import me.desht.pneumaticcraft.common.block.entity.FluidMixerBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class FluidMixerBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {

    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(7, 13, 4, 12, 16, 5),
            Block.box(0, 10, 14, 2, 13, 16),
            Block.box(0, 10, 0, 2, 13, 2),
            Block.box(14, 10, 0, 16, 13, 2),
            Block.box(0.5, 9, 0.5, 1.5, 15, 1.5),
            Block.box(14.5, 1, 14.5, 15.5, 15, 15.5),
            Block.box(0.5, 1, 14.5, 1.5, 15, 15.5),
            Block.box(0.5, 11, 2, 1.5, 12, 14.5),
            Block.box(14.5, 11, 2, 15.5, 12, 14.5),
            Block.box(15, 15, 1, 16, 16, 15),
            Block.box(0, 15, 1, 1, 16, 15),
            Block.box(0, 15, 15, 16, 16, 16),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(9, 1, 0, 16, 9, 5),
            Block.box(0, 1, 0, 7, 9, 5),
            Block.box(14, 1, 5, 16, 9, 6),
            Block.box(0, 1, 5, 2, 9, 6),
            Block.box(2, 1, 5, 14, 16, 15),
            Block.box(14, 10, 14, 16, 13, 16),
            Block.box(7, 10, 15, 12, 13, 16),
            Block.box(14.5, 9, 0.5, 15.5, 15, 1.5),
            Block.box(7, 2.5, 1.5, 9, 3.5, 2.5),
            Block.box(10, 3, 15, 11, 10, 16),
            Block.box(8, 6, 15, 9, 10, 16),
            Block.box(10, 13, 15, 11, 15, 16),
            Block.box(8, 13, 15, 9, 15, 16),
            Block.box(8.75, 2, 1, 9, 4, 3),
            Block.box(7, 2, 1, 7.25, 4, 3),
            Block.box(9.5, 2.5, 15, 11.5, 4.5, 15.25),
            Block.box(7.5, 5.5, 15, 9.5, 7.5, 15.25),
            Block.box(14, 5, 12, 14.25, 7, 14),
            Block.box(14, 3.5, 9, 14.25, 5.5, 11),
            Block.box(0.5, 5.5, 5.5, 1.5, 6.5, 12.5),
            Block.box(0.5, 5.5, 12.5, 1.75, 6.5, 13.5),
            Block.box(1.75, 5, 12, 2, 7, 14),
            Block.box(1.75, 3.5, 9, 2, 5.5, 11),
            Block.box(0.5, 4, 9.5, 1.75, 5, 10.5),
            Block.box(0.5, 4, 5.5, 1.5, 5, 9.5),
            Block.box(14.5, 4, 5.5, 15.5, 5, 9.5),
            Block.box(14.25, 4, 9.5, 15.5, 5, 10.5),
            Block.box(14.5, 5.5, 5.5, 15.5, 6.5, 12.5),
            Block.box(14.25, 5.5, 12.5, 15.5, 6.5, 13.5),
            Block.box(0, 15, 0, 2, 16, 1),
            Block.box(14, 15, 0, 16, 16, 1),
            Block.box(12, 11, 14.5, 14, 12, 15.5),
            Block.box(2, 11, 14.5, 7, 12, 15.5),
            Block.box(2, 10, 0, 14, 16, 5)
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public FluidMixerBlock() {
        super(ModBlocks.defaultProps());
        registerDefaultState(defaultBlockState()
                .setValue(AbstractPneumaticCraftBlock.NORTH, false)
                .setValue(AbstractPneumaticCraftBlock.SOUTH, false)
                .setValue(AbstractPneumaticCraftBlock.WEST, false)
                .setValue(AbstractPneumaticCraftBlock.EAST, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AbstractPneumaticCraftBlock.NORTH, AbstractPneumaticCraftBlock.SOUTH, AbstractPneumaticCraftBlock.WEST, AbstractPneumaticCraftBlock.EAST);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FluidMixerBlockEntity(pPos, pState);
    }
}
