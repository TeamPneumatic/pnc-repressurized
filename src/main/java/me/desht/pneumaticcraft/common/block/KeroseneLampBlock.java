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

import me.desht.pneumaticcraft.common.block.entity.KeroseneLampBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class KeroseneLampBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_NS = VoxelShapeUtils.or(
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
    );
    private static final VoxelShape SHAPE_EW = VoxelShapeUtils.rotateY(SHAPE_NS, 90);

    public static final EnumProperty<Direction> CONNECTED = EnumProperty.create("connected", Direction.class);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public KeroseneLampBlock() {
        super(ModBlocks.defaultProps().lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        registerDefaultState(getStateDefinition().any().setValue(LIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return getRotation(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTED, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(CONNECTED, getConnectedDirection(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return stateIn.setValue(CONNECTED, getConnectedDirection(worldIn, currentPos));
    }

    private Direction getConnectedDirection(LevelAccessor world, BlockPos pos) {
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

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new KeroseneLampBlockEntity(pPos, pState);
    }

    public static class ItemBlockKeroseneLamp extends BlockItem implements ICustomTooltipName {
        public ItemBlockKeroseneLamp(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return ConfigHelper.common().machines.keroseneLampCanUseAnyFuel.get() ? getDescriptionId() : getDescriptionId() + ".kerosene_only";
        }
    }
}
