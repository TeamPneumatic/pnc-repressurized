package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockEtchingTank extends BlockPneumaticCraft {
    private static final VoxelShape BASE = makeCuboidShape(1, 0, 1, 15, 10, 15);
    private static final VoxelShape TOP = makeCuboidShape(2, 10, 2, 14, 16, 14);
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(BASE, TOP, IBooleanFunction.OR);

    public BlockEtchingTank() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityEtchingTank.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BlockPneumaticCraft.ALMOST_FULL_SHAPE;
    }
}
