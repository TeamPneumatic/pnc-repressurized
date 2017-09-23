package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockUniversalSensor extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.UNIVERSAL_SENSOR_MIN_POS, 0F, BBConstants.UNIVERSAL_SENSOR_MIN_POS,
            BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS,
            BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS
    );

    BlockUniversalSensor() {
        super(Material.IRON, "universal_sensor");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

//    @Override
//    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, BlockPos pos) {
//        setBlockBounds(BBConstants.UNIVERSAL_SENSOR_MIN_POS, 0F, BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS);
//    }
//
//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBounds(BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MIN_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS, BBConstants.UNIVERSAL_SENSOR_MAX_POS_TOP, BBConstants.UNIVERSAL_SENSOR_MAX_POS);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityUniversalSensor.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.UNIVERSAL_SENSOR;
    }

    protected boolean isRotable() {
        return true;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        return te instanceof TileEntityUniversalSensor ? ((TileEntityUniversalSensor) te).redstoneStrength : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }
}
