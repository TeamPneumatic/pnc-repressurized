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

import me.desht.pneumaticcraft.common.block.entity.AirCompressorBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class AirCompressorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(1.5, 11, 9.25, 3.5, 13, 10.25),
            Block.box(11.5, 12, 10, 12.5, 13, 11),
            Block.box(13, 12, 10, 14, 13, 11),
            Block.box(14.5, 12, 10, 15.5, 13, 13),
            Block.box(4, 11, 1, 12, 12, 2),
            Block.box(4.75, 11, 12.5, 5.75, 13, 14.5),
            Block.box(2, 11.5, 13, 5, 12.5, 14),
            Block.box(2, 11.5, 10, 3, 12.5, 13),
            Block.box(0, 11, 2, 16, 16, 10),
            Block.box(1.5, 2.5, 10.25, 3.5, 4.5, 11.25),
            Block.box(4.75, 2.5, 12.5, 5.75, 4.5, 14.5),
            Block.box(2, 3, 11, 3, 4, 13),
            Block.box(2, 3, 13, 5, 4, 14),
            Block.box(1.5, 5.5, 10.25, 3.5, 7.5, 11.25),
            Block.box(4.75, 5.5, 12.5, 5.75, 7.5, 14.5),
            Block.box(2, 6, 11, 3, 7, 13),
            Block.box(2, 6, 13, 5, 7, 14),
            Block.box(4, 1, 0, 12, 12, 1),
            Block.box(5, 1, 11, 15, 14, 15),
            Block.box(0, 1, 1, 16, 11, 11),
            Block.box(0, 0, 0, 16, 1, 16)
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public AirCompressorBlock() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ON);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AirCompressorBlockEntity(pPos, pState);
    }
}
