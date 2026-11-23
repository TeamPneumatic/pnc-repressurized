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
import me.desht.pneumaticcraft.common.block.entity.hopper.AbstractHopperBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.hopper.OmnidirectionalHopperBlockEntity;
import me.desht.pneumaticcraft.common.item.PNCBlockItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class OmnidirectionalHopperBlock extends AbstractPneumaticCraftBlock
        implements ColorHandlers.ITintableBlock, PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    private static final VoxelShape MIDDLE_SHAPE = Block.box(4, 4, 4, 12, 10, 12);
    private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape INPUT_MIDDLE_SHAPE = Shapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
    private static final VoxelShape BOWL_SHAPE = Block.box(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape OUTPUT_DOWN_SHAPE = Shapes.or(
            Block.box(6, 3, 6, 10, 4, 10),
            Block.box(6.5, 0, 6.5, 9.5, 4, 9.5));

    public static final Map<Direction,VoxelShape> INPUT_SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, Shapes.join(INPUT_MIDDLE_SHAPE, BOWL_SHAPE, BooleanOp.ONLY_FIRST));
        map.put(Direction.NORTH, VoxelShapeUtils.rotateX(map.get(Direction.UP), 270));
        map.put(Direction.DOWN, VoxelShapeUtils.rotateX(map.get(Direction.UP), 180));
        map.put(Direction.SOUTH, VoxelShapeUtils.rotateX(map.get(Direction.UP), 90));
        map.put(Direction.WEST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 270));
        map.put(Direction.EAST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 90));
    });
    private static final Map<Direction,VoxelShape> INPUT_SHAPES_INTERACT = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.UP, INPUT_MIDDLE_SHAPE);
        map.put(Direction.DOWN, VoxelShapeUtils.rotateX(map.get(Direction.UP), 180));
        map.put(Direction.NORTH, VoxelShapeUtils.rotateX(map.get(Direction.UP), 270));
        map.put(Direction.SOUTH, VoxelShapeUtils.rotateX(map.get(Direction.UP), 90));
        map.put(Direction.WEST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 270));
        map.put(Direction.EAST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 90));
    });
    private static final Map<Direction,VoxelShape> OUTPUT_SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.DOWN, OUTPUT_DOWN_SHAPE);
        map.put(Direction.UP, VoxelShapeUtils.rotateX(map.get(Direction.DOWN), 180));
        map.put(Direction.NORTH, VoxelShapeUtils.rotateX(map.get(Direction.DOWN), 90));
        map.put(Direction.SOUTH, VoxelShapeUtils.rotateX(map.get(Direction.DOWN), 270));
        map.put(Direction.WEST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 270));
        map.put(Direction.EAST, VoxelShapeUtils.rotateY(map.get(Direction.NORTH), 90));
    });

    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[36];
    private static final VoxelShape[] INTERACTION_SHAPE_CACHE = new VoxelShape[36];

    // standard FACING property is used for the output direction
    public static final EnumProperty<Direction> INPUT_FACING = EnumProperty.create("input", Direction.class);

    public OmnidirectionalHopperBlock(Properties props) {
        super(props);
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return getCachedShape(state, SHAPE_CACHE, INPUT_SHAPES);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getCachedShape(state, INTERACTION_SHAPE_CACHE, INPUT_SHAPES_INTERACT);
    }

    private VoxelShape getCachedShape(BlockState state, VoxelShape[] cache, Map<Direction, VoxelShape> inputMap) {
        int idx = state.getValue(INPUT_FACING).get3DDataValue() + state.getValue(directionProperty()).get3DDataValue() * 6;
        if (cache[idx] == null) {
            cache[idx] = Shapes.join(
                    inputMap.get(state.getValue(INPUT_FACING)),
                    OUTPUT_SHAPES.get(state.getValue(directionProperty())),
                    BooleanOp.OR);
        }
        return cache[idx];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INPUT_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ?
                null :
                state.setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite())
                        .setValue(INPUT_FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    private Direction getInputDirection(Level world, BlockPos pos) {
        return world.getBlockState(pos).getValue(OmnidirectionalHopperBlock.INPUT_FACING);
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction face, InteractionHand hand) {
        BlockState state = world.getBlockState(pos);
        if (player != null && player.isShiftKeyDown()) {
            Direction outputDir = getRotation(state);
            outputDir = Direction.from3DDataValue(outputDir.get3DDataValue() + 1);
            if (outputDir == getInputDirection(world, pos)) outputDir = Direction.from3DDataValue(outputDir.get3DDataValue() + 1);
            setRotation(world, pos, outputDir);
        } else {
            Direction inputDir = state.getValue(INPUT_FACING);
            inputDir = Direction.from3DDataValue(inputDir.get3DDataValue() + 1);
            if (inputDir == getRotation(world, pos)) inputDir = Direction.from3DDataValue(inputDir.get3DDataValue() + 1);
            world.setBlockAndUpdate(pos, state.setValue(INPUT_FACING, inputDir));
        }
        PneumaticCraftUtils.getBlockEntityAt(world, pos, AbstractHopperBlockEntity.class).ifPresent(AbstractHopperBlockEntity::onBlockRotated);
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable BlockAndLightGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world instanceof LevelReader reader && pos != null) {
            switch (tintIndex) {
                case 0:
                    return PneumaticCraftUtils.getBlockEntityAt(reader, pos, AbstractHopperBlockEntity.class)
                            .filter(te -> te.isCreative)
                            .map(te -> 0xFFDB46CF).orElse(0xFF2b2727);
                case 1:
                    return 0xFFA0A0A0;
            }
        }
        return 0xFFFFFFFF;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new OmnidirectionalHopperBlockEntity(pPos, pState);
    }

    public static class ItemBlockOmnidirectionalHopper extends PNCBlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockOmnidirectionalHopper(Block block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            int n = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.CREATIVE.get());
            return n > 0 ? 0xFFDB46CF : 0xFF2b2727;
        }
    }
}
