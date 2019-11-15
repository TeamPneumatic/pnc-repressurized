package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockProgrammer extends BlockPneumaticCraftModeled {
    private static final VoxelShape BODY = Block.makeCuboidShape(0, 8, 0, 16, 11, 16);
    private static final VoxelShape LEG1 = Block.makeCuboidShape(0, 0, 0, 1, 8, 1);
    private static final VoxelShape LEG2 = Block.makeCuboidShape(15, 0, 15, 16, 8, 16);
    private static final VoxelShape LEG3 = Block.makeCuboidShape(0, 0, 15, 1, 8, 16);
    private static final VoxelShape LEG4 = Block.makeCuboidShape(15, 0, 0, 16, 8, 1);
    private static final VoxelShape SHAPE = VoxelShapes.or(BODY, LEG1, LEG2, LEG3, LEG4);

    public BlockProgrammer() {
        super("programmer");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityProgrammer.class;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityProgrammer) {
                ((TileEntityProgrammer) te).sendDescriptionPacket();
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, brtr);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
