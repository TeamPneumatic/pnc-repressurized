package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAbstractHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockOmnidirectionalHopper extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {

    private static final VoxelShape MIDDLE_SHAPE = Block.box(4, 6, 4, 12, 10, 12);
    private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape INPUT_MIDDLE_SHAPE = VoxelShapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
    private static final VoxelShape BOWL_SHAPE = Block.box(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final VoxelShape INPUT_UP    = VoxelShapes.join(INPUT_MIDDLE_SHAPE, BOWL_SHAPE, IBooleanFunction.ONLY_FIRST);
    private static final VoxelShape INPUT_NORTH = VoxelShapeUtils.rotateX(INPUT_UP, 270);
    private static final VoxelShape INPUT_DOWN  = VoxelShapeUtils.rotateX(INPUT_NORTH, 270);
    private static final VoxelShape INPUT_SOUTH = VoxelShapeUtils.rotateX(INPUT_UP, 90);
    private static final VoxelShape INPUT_WEST  = VoxelShapeUtils.rotateY(INPUT_NORTH, 270);
    private static final VoxelShape INPUT_EAST  = VoxelShapeUtils.rotateY(INPUT_NORTH, 90);
    public static final VoxelShape[] INPUT_SHAPES = {
        INPUT_DOWN, INPUT_UP, INPUT_NORTH, INPUT_SOUTH, INPUT_WEST, INPUT_EAST
    };

    private static final VoxelShape OUTPUT_DOWN = Block.box(6.5, 0, 6.5, 9.5, 6, 9.5);
    private static final VoxelShape OUTPUT_UP = Block.box(6.5, 10, 6.5, 9.5, 16, 9.5);
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

    public BlockOmnidirectionalHopper() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityOmnidirectionalHopper.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int idx = state.getValue(INPUT_FACING).get3DDataValue() + state.getValue(directionProperty()).get3DDataValue() * 6;
        if (SHAPE_CACHE[idx] == null) {
            SHAPE_CACHE[idx] = VoxelShapes.join(
                    INPUT_SHAPES[state.getValue(INPUT_FACING).get3DDataValue()],
                    OUTPUT_SHAPES[state.getValue(directionProperty()).get3DDataValue()],
                    IBooleanFunction.OR);
        }
        return SHAPE_CACHE[idx];
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INPUT_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite())
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

    private Direction getInputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(BlockOmnidirectionalHopper.INPUT_FACING);
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
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
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityAbstractHopper.class).ifPresent(TileEntityAbstractHopper::onBlockRotated);
        return true;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            switch (tintIndex) {
                case 0:
                    return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityAbstractHopper.class)
                            .filter(te -> te.isCreative)
                            .map(te -> 0xFFFF80FF).orElse(0xFFFFFFFF);
                case 1:
                    return 0xFFA0A0A0;
            }
        }
        return 0xFFFFFFFF;
    }

    public static class ItemBlockOmnidirectionalHopper extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockOmnidirectionalHopper(Block block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            int n = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.CREATIVE);
            return n > 0 ? 0xFFFF60FF : 0xFFFFFFFF;
        }
    }
}
