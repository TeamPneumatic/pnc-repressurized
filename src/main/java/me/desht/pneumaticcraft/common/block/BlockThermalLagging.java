package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class BlockThermalLagging extends BlockPneumaticCraft {
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(0, 0, 0, 16,  2, 16),
            Block.box(0, 14, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16,  2),
            Block.box(0, 0, 14, 16, 16, 16),
            Block.box(0, 0, 0,  2, 16, 16),
            Block.box(14, 0, 0, 16, 16, 16),
    };

    public BlockThermalLagging() {
        super(ModBlocks.defaultProps().noOcclusion().noCollission());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        if (selectionContext.getEntity() instanceof LivingEntity) {
            ItemStack stack = ((LivingEntity) selectionContext.getEntity()).getMainHandItem();
            return ModdedWrenchUtils.getInstance().isWrench(stack)
                    || stack.getToolTypes().contains(ToolType.PICKAXE)
                    || selectionContext.getEntity().isShiftKeyDown() ?
                    SHAPES[getRotation(state).get3DDataValue()] : VoxelShapes.empty();
        }
        return SHAPES[getRotation(state).get3DDataValue()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return defaultBlockState().setValue(directionProperty(), ctx.getClickedFace().getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction dir = getRotation(state);
        return !worldIn.getBlockState(pos.relative(dir)).isAir(worldIn, pos.relative(dir));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!canSurvive(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getBlock() == state.getBlock();
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }
}
