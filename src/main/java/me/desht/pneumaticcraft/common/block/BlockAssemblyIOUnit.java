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

    public BlockAssemblyIOUnit(boolean isImport, String registryName) {
        super(registryName);
        this.isImport = isImport;
    }

//    @Override
//    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
//        if (player != null && player.isSneaking()) {
//            return super.onWrenched(world, player, pos, side, hand);
//        } else {
//            // flip between import and export
//            BlockState state = world.getBlockState(pos);
//            boolean isImport = state.get(IMPORT_UNIT);
//            world.setBlockState(pos, state.with(IMPORT_UNIT, !isImport));
//            TileEntity te = world.getTileEntity(pos);
//            if (te instanceof TileEntityAssemblyIOUnit) {
//                ((TileEntityAssemblyIOUnit) te).switchMode();
//            }
//            return true;
//        }
//    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyIOUnit.class;
    }

    public static class Import extends BlockAssemblyIOUnit {
        public Import() {
            super(true, "assembly_io_unit_import");
        }
    }

    public static class Export extends BlockAssemblyIOUnit {
        public Export() {
            super(false, "assembly_io_unit_export");
        }
    }
}
