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
    private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 1, 16);
    private static final VoxelShape SHAPE = VoxelShapes.or(BASE_SHAPE, Block.makeCuboidShape(5, 1, 5, 11, 7, 11));

    private final boolean isImport;

    public BlockAssemblyIOUnit(Properties props, boolean isImport) {
        super(props);
        this.isImport = isImport;
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
            super(props,true);
        }
    }

    public static class Export extends BlockAssemblyIOUnit {
        public Export(Properties props) {
            super(props,false);
        }
    }
}
