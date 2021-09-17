package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static net.minecraft.state.properties.BlockStateProperties.LIT;

public class BlockWallLamp extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    private static final VoxelShape SHAPE_UP = Stream.of(
            Block.box(3, 0, 3, 13, 1, 13),
            Block.box(4, 1, 4, 12, 2, 12),
            Block.box(6.15, 2.25, 4.75, 6.65, 3.5, 11.25),
            Block.box(5, 2.25, 5, 11, 3.25, 11),
            Block.box(4.75, 1.25, 4.75, 11.25, 2.5, 11.25),
            Block.box(9.35, 2.25, 4.75, 9.85, 3.5, 11.25),
            Block.box(4.75, 2.25, 9.35, 11.25, 3.5, 9.85),
            Block.box(4.75, 2.25, 6.15, 11.25, 3.5, 6.65)
        ).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get();
    private static final VoxelShape SHAPE_NORTH = VoxelShapeUtils.rotateX(SHAPE_UP, 270);
    private static final VoxelShape SHAPE_DOWN = VoxelShapeUtils.rotateX(SHAPE_NORTH, 270);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapeUtils.rotateX(SHAPE_UP, 90);
    private static final VoxelShape SHAPE_WEST = VoxelShapeUtils.rotateY(SHAPE_NORTH, 270);
    private static final VoxelShape SHAPE_EAST = VoxelShapeUtils.rotateY(SHAPE_NORTH, 90);
    private static final VoxelShape[] SHAPES = { SHAPE_DOWN, SHAPE_UP, SHAPE_NORTH, SHAPE_SOUTH, SHAPE_WEST, SHAPE_EAST };

    private static final int[] DARKENED = new int[DyeColor.values().length];
    static {
        for (DyeColor c : DyeColor.values()) {
            TintColor tc = new TintColor(c.getColorValue()).darker();
            DARKENED[c.getId()] = tc.getRGB();
        }
    }

    private final DyeColor color;
    private final boolean inverted;

    public BlockWallLamp(DyeColor color, boolean inverted) {
        super(ModBlocks.defaultProps().lightLevel(getLightValue()));

        this.color = color;
        this.inverted = inverted;

        registerDefaultState(getStateDefinition().any().setValue(LIT, inverted));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.getValue(directionProperty()).get3DDataValue()];
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (state.getValue(LIT) && !shouldLight(worldIn, pos)) {
            worldIn.setBlock(pos, state.cycle(LIT), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            boolean isLit = state.getValue(LIT);
            if (isLit != shouldLight(worldIn, pos)) {
                if (isLit) {
                    worldIn.getBlockTicks().scheduleTick(pos, this, 4);
                } else {
                    worldIn.setBlock(pos, state.cycle(LIT), Constants.BlockFlags.BLOCK_UPDATE);
                }
            }
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return defaultBlockState()
                .setValue(directionProperty(), context.getClickedFace())
                .setValue(LIT, shouldLight(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return getRotation(stateIn).getOpposite() == facing && !stateIn.canSurvive(worldIn, currentPos) ?
                Blocks.AIR.defaultBlockState() :
                super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return HorizontalFaceBlock.canAttach(worldIn, pos, getRotation(state).getOpposite());
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 1 && state != null) {
            return state.getValue(LIT) ? color.getColorValue() : DARKENED[color.ordinal()];
        }
        return 0xFFFFFFFF;
    }

    private boolean shouldLight(World world, BlockPos pos) {
        return inverted != world.hasNeighborSignal(pos);
    }

    private static ToIntFunction<BlockState> getLightValue() {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? 15 : 0;
    }

    public static class ItemWallLamp extends BlockItem implements ICustomTooltipName {
        public ItemWallLamp(BlockWallLamp blockWallLamp) {
            super(blockWallLamp, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            if (getBlock() instanceof BlockWallLamp) {
                return ((BlockWallLamp) getBlock()).inverted ? "block.pneumaticcraft.wall_lamp_inverted" : "block.pneumaticcraft.wall_lamp";
            } else {
                // shouldn't happen
                return "block.pneumaticcraft.wall_lamp";
            }
        }
    }
}
