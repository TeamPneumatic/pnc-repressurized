package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class BlockPressureChamberGlass extends BlockPressureChamberWallBase {
    public BlockPressureChamberGlass() {
        super(IBlockPressureChamber.pressureChamberBlockProps().notSolid());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberGlass.class;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (worldIn.isRemote()) {
            PneumaticCraftUtils.getTileEntityAt(worldIn, currentPos, TileEntityPressureChamberGlass.class).ifPresent(teGlass -> {
                teGlass.requestModelDataUpdate();
                // handle any glass that's diagonally connected
                for (Direction d : DirectionUtil.VALUES) {
                    if (d.getAxis() != facing.getAxis()) {
                        TileEntity te1 = teGlass.getCachedNeighbor(d);
                        if (te1 instanceof TileEntityPressureChamberGlass) te1.requestModelDataUpdate();
                    }
                }
            });
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isSideInvisible(BlockState ourState, BlockState theirState, Direction side) {
        return ourState.getBlock() == theirState.getBlock() || super.isSideInvisible(ourState, theirState, side);
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState p_220080_1_, IBlockReader p_220080_2_, BlockPos p_220080_3_) {
        return 0.2F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

//    @Override
//    public boolean causesSuffocation(BlockState p_220060_1_, IBlockReader p_220060_2_, BlockPos p_220060_3_) {
//        return false;
//    }

//    @Override
//    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
//        return false;
//    }

//    public boolean canEntitySpawn(BlockState p_220067_1_, IBlockReader p_220067_2_, BlockPos p_220067_3_, EntityType<?> p_220067_4_) {
//        return false;
//    }

}
