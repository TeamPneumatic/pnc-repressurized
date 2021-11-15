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

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
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

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockKeroseneLamp extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_NS = Stream.of(
            Block.box(5, 0, 5, 11, 1, 11),
            Block.box(5, 9, 5, 11, 10, 11),
            Block.box(11.5, 0, 7, 12.5, 10, 9),
            Block.box(11, 0, 7, 12, 1, 9),
            Block.box(11, 9, 7, 12, 10, 9),
            Block.box(4, 0, 7, 5, 1, 9),
            Block.box(4, 9, 7, 5, 10, 9),
            Block.box(3.5, 0, 7, 4.5, 10, 9),
            Block.box(6, 10, 6, 10, 11, 10),
            Block.box(5, 1, 5, 11, 9, 11)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
    private static final VoxelShape SHAPE_EW = VoxelShapeUtils.rotateY(SHAPE_NS, 90);

    public static final EnumProperty<Direction> CONNECTED = EnumProperty.create("connected", Direction.class);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public BlockKeroseneLamp() {
        super(ModBlocks.defaultProps().lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return getRotation(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTED, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(CONNECTED, getConnectedDirection(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return stateIn.setValue(CONNECTED, getConnectedDirection(worldIn, currentPos));
    }

    private Direction getConnectedDirection(IWorld world, BlockPos pos) {
        Direction connectedDir = Direction.DOWN;
        for (Direction d : DirectionUtil.VALUES) {
            BlockPos neighborPos = pos.relative(d);
            if (Block.canSupportCenter(world, neighborPos, d.getOpposite())) {
                connectedDir = d;
                break;
            }
        }
        return connectedDir;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityKeroseneLamp.class;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    public static class ItemBlockKeroseneLamp extends BlockItem implements ICustomTooltipName {
        public ItemBlockKeroseneLamp(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return PNCConfig.Common.Machines.keroseneLampCanUseAnyFuel ? getDescriptionId() : getDescriptionId() + ".kerosene_only";
        }
    }
}
