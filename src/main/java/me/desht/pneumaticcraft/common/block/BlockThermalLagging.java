package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
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
            Block.makeCuboidShape(0, 0, 0, 16,  2, 16),
            Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0, 16, 16,  2),
            Block.makeCuboidShape(0, 0, 14, 16, 16, 16),
            Block.makeCuboidShape(0, 0, 0,  2, 16, 16),
            Block.makeCuboidShape(14, 0, 0, 16, 16, 16),
    };

    public BlockThermalLagging() {
        super(ModBlocks.defaultProps().notSolid().doesNotBlockMovement());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        if (selectionContext.getEntity() instanceof LivingEntity) {
            ItemStack stack = ((LivingEntity) selectionContext.getEntity()).getHeldItemMainhand();
            return stack.getItem() == ModItems.PNEUMATIC_WRENCH.get()
                    || ModdedWrenchUtils.getInstance().isModdedWrench(stack)
                    || stack.getToolTypes().contains(ToolType.PICKAXE)
                    || selectionContext.getEntity().isSneaking() ?
                    SHAPES[getRotation(state).getIndex()] : VoxelShapes.empty();
        }
        return SHAPES[getRotation(state).getIndex()];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getDefaultState().with(directionProperty(), ctx.getFace().getOpposite());
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction dir = getRotation(state);
        return !worldIn.getBlockState(pos.offset(dir)).isAir(worldIn, pos.offset(dir));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!isValidPosition(stateIn, worldIn, currentPos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
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
