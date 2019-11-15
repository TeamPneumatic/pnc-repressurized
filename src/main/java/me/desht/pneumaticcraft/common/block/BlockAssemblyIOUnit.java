package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockAssemblyIOUnit extends BlockPneumaticCraft {
    public static final BooleanProperty IMPORT_UNIT = BooleanProperty.create("import");

    private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(2, 0, 2, 14, 14, 14);
    private static final VoxelShape COLLISION_SHAPE = Block.makeCuboidShape(2, 2, 2, 14, 14, 14);

    public BlockAssemblyIOUnit() {
        super("assembly_io_unit");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(IMPORT_UNIT);
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand) {
        if (player != null && player.isSneaking()) {
            return super.onWrenched(world, player, pos, side, hand);
        } else {
            // flip between import and export
            BlockState state = world.getBlockState(pos);
            boolean isImport = state.get(IMPORT_UNIT);
            world.setBlockState(pos, state.with(IMPORT_UNIT, !isImport));
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityAssemblyIOUnit) {
                ((TileEntityAssemblyIOUnit) te).switchMode();
            }
            return true;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return BASE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return COLLISION_SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyIOUnit.class;
    }

}
