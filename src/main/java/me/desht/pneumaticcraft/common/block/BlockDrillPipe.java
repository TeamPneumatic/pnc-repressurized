package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockDrillPipe extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE1 = Block.makeCuboidShape(6, 0, 7, 10, 16, 9);
    private static final VoxelShape SHAPE2 = Block.makeCuboidShape(7, 0, 6, 9, 16, 10);
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(SHAPE1, SHAPE2, IBooleanFunction.OR);

    public BlockDrillPipe() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }
}
