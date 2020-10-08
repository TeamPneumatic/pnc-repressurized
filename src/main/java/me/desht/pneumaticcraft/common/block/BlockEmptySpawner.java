package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockEmptySpawner extends BlockPneumaticCraft {
    public BlockEmptySpawner() {
        super(ModBlocks.defaultProps().notSolid());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return ALMOST_FULL_SHAPE;
    }
}
