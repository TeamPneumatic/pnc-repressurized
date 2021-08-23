package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public abstract class BlockAssemblyIOUnit extends BlockPneumaticCraft {
    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape SHAPE = VoxelShapes.or(BASE_SHAPE, Block.box(5, 1, 5, 11, 7, 11));

    public BlockAssemblyIOUnit(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyIOUnit.class;
    }

    public static class Import extends BlockAssemblyIOUnit {
        public Import(Properties props) {
            super(props);
        }
    }

    public static class Export extends BlockAssemblyIOUnit {
        public Export(Properties props) {
            super(props);
        }
    }
}
