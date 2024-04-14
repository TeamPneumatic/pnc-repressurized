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
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class OmnidirectionalHopperBlock extends AbstractPneumaticCraftBlock
        implements ColorHandlers.ITintableBlock, PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    private static final VoxelShape MIDDLE_SHAPE = Block.box(4, 4, 4, 12, 10, 12);
    private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape INPUT_MIDDLE_SHAPE = Shapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
    private static final VoxelShape BOWL_SHAPE = Block.box(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final VoxelShape INPUT_UP    = Shapes.join(INPUT_MIDDLE_SHAPE, BOWL_SHAPE, BooleanOp.ONLY_FIRST);
    private static final VoxelShape INPUT_NORTH = VoxelShapeUtils.rotateX(INPUT_UP, 270);
    private static final VoxelShape INPUT_DOWN  = VoxelShapeUtils.rotateX(INPUT_NORTH, 270);
    private static final VoxelShape INPUT_SOUTH = VoxelShapeUtils.rotateX(INPUT_UP, 90);
    private static final VoxelShape INPUT_WEST  = VoxelShapeUtils.rotateY(INPUT_NORTH, 270);
    private static final VoxelShape INPUT_EAST  = VoxelShapeUtils.rotateY(INPUT_NORTH, 90);
    public static final VoxelShape[] INPUT_SHAPES = {
            INPUT_DOWN, INPUT_UP, INPUT_NORTH, INPUT_SOUTH, INPUT_WEST, INPUT_EAST
    };

    private static final VoxelShape OUTPUT_DOWN = Shapes.join(Block.box(6, 3, 6, 10, 4, 10), Block.box(6.5, 0, 6.5, 9.5, 4, 9.5), BooleanOp.OR);
    private static final VoxelShape OUTPUT_UP = Shapes.join(Block.box(6, 12, 6, 10, 13, 10), Block.box(6.5, 12, 6.5, 9.5, 16, 9.5), BooleanOp.OR);
    private static final VoxelShape OUTPUT_NORTH = VoxelShapeUtils.rotateX(OUTPUT_DOWN, 90);
    private static final VoxelShape OUTPUT_SOUTH = VoxelShapeUtils.rotateX(OUTPUT_DOWN, 270);
    private static final VoxelShape OUTPUT_WEST = VoxelShapeUtils.rotateY(OUTPUT_NORTH, 270);
    private static final VoxelShape OUTPUT_EAST = VoxelShapeUtils.rotateY(OUTPUT_NORTH, 90);
    private static final VoxelShape[] OUTPUT_SHAPES = {
            OUTPUT_DOWN, OUTPUT_UP, OUTPUT_NORTH, OUTPUT_SOUTH, OUTPUT_WEST, OUTPUT_EAST
    };
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[36];

    // standard FACING property is used for the output direction
    public static final EnumProperty<Direction> INPUT_FACING = EnumProperty.create("input", Direction.class);

    public OmnidirectionalHopperBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        int idx = state.getValue(INPUT_FACING).get3DDataValue() + state.getValue(directionProperty()).get3DDataValue() * 6;
        if (SHAPE_CACHE[idx] == null) {
            SHAPE_CACHE[idx] = Shapes.join(
                    INPUT_SHAPES[state.getValue(INPUT_FACING).get3DDataValue()],
                    OUTPUT_SHAPES[state.getValue(directionProperty()).get3DDataValue()],
                    BooleanOp.OR);
        }
        return SHAPE_CACHE[idx];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INPUT_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return state;
        return state.setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite())
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
        PneumaticCraftUtils.getTileEntityAt(world, pos, AbstractHopperBlockEntity.class).ifPresent(AbstractHopperBlockEntity::onBlockRotated);
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            switch (tintIndex) {
                case 0:
                    return PneumaticCraftUtils.getTileEntityAt(world, pos, AbstractHopperBlockEntity.class)
                            .filter(te -> te.isCreative)
                            .map(te -> 0xFFDB46CF).orElse(0xFF2b2727);
                case 1:
                    return 0xFFA0A0A0;
            }
        }
        return 0xFFFFFFFF;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new OmnidirectionalHopperBlockEntity(pPos, pState);
    }

    public static class ItemBlockOmnidirectionalHopper extends BlockItem implements ColorHandlers.ITintableItem {
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
