package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockPressureChamberGlass extends BlockPressureChamberWallBase {
    public BlockPressureChamberGlass() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(3f, 20000f), "pressure_chamber_glass");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberGlass.class;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (worldIn.isRemote()) {
            TileEntity te = worldIn.getTileEntity(currentPos);
            if (te instanceof TileEntityPressureChamberGlass) {
                te.requestModelDataUpdate();
                // handle any glass that's diagonally connected
                for (Direction d : Direction.VALUES) {
                    if (d.getAxis() != facing.getAxis()) {
                        TileEntity te1 = ((TileEntityPressureChamberGlass) te).getCachedNeighbor(d);
                        if (te1 instanceof TileEntityPressureChamberGlass) te1.requestModelDataUpdate();
                    }
                }
            }
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isSideInvisible(BlockState ourState, BlockState theirState, Direction side) {
        return ourState.getBlock() == theirState.getBlock() || super.isSideInvisible(ourState, theirState, side);
    }

    @OnlyIn(Dist.CLIENT)
    public float func_220080_a(BlockState p_220080_1_, IBlockReader p_220080_2_, BlockPos p_220080_3_) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    public boolean causesSuffocation(BlockState p_220060_1_, IBlockReader p_220060_2_, BlockPos p_220060_3_) {
        return false;
    }

    public boolean canEntitySpawn(BlockState p_220067_1_, IBlockReader p_220067_2_, BlockPos p_220067_3_, EntityType<?> p_220067_4_) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

}
