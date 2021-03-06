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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.ToIntFunction;

import static net.minecraft.state.properties.BlockStateProperties.LIT;

public class BlockWallLamp extends BlockPneumaticCraft implements ColorHandlers.ITintableBlock {
    private static final VoxelShape BASE1 = makeCuboidShape(3, 0, 2, 13, 1, 14);
    private static final VoxelShape BASE2 = makeCuboidShape(2, 0, 3, 14, 1, 13);
    private static final VoxelShape BASE3 = makeCuboidShape(4, 1, 3, 12, 2, 13);
    private static final VoxelShape BASE4 = makeCuboidShape(3, 1, 4, 13, 2, 12);
    private static final VoxelShape SHAPE_UP = VoxelShapes.or(BASE1, BASE2, BASE3, BASE4);
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
        super(ModBlocks.defaultProps().setLightLevel(getLightValue()));

        this.color = color;
        this.inverted = inverted;

        setDefaultState(getStateContainer().getBaseState().with(LIT, inverted));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.get(directionProperty()).getIndex()];
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (state.get(LIT) && !shouldLight(worldIn, pos)) {
            worldIn.setBlockState(pos, state.func_235896_a_(LIT), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            boolean isLit = state.get(LIT);
            if (isLit != shouldLight(worldIn, pos)) {
                if (isLit) {
                    worldIn.getPendingBlockTicks().scheduleTick(pos, this, 4);
                } else {
                    worldIn.setBlockState(pos, state.func_235896_a_(LIT), Constants.BlockFlags.BLOCK_UPDATE);
                }
            }
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState()
                .with(directionProperty(), context.getFace())
                .with(LIT, shouldLight(context.getWorld(), context.getPos()));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return getRotation(stateIn).getOpposite() == facing && !stateIn.isValidPosition(worldIn, currentPos) ?
                Blocks.AIR.getDefaultState() :
                super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return HorizontalFaceBlock.isSideSolidForDirection(worldIn, pos, getRotation(state).getOpposite());
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
            return state.get(LIT) ? color.getColorValue() : DARKENED[color.ordinal()];
        }
        return 0xFFFFFFFF;
    }

    private boolean shouldLight(World world, BlockPos pos) {
        return inverted != world.isBlockPowered(pos);
    }

    private static ToIntFunction<BlockState> getLightValue() {
        return (state) -> state.get(BlockStateProperties.LIT) ? 15 : 0;
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
