package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockVacuumPump extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BASE_BOUNDS = new AxisAlignedBB(
            BBConstants.VACUUM_PUMP_MIN_POS, 0F, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MAX_POS,
            BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);
    private static final AxisAlignedBB COLL_BOUNDS = new AxisAlignedBB(
            BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS,
            BBConstants.VACUUM_PUMP_MAX_POS, BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);


    BlockVacuumPump() {
        super(Material.IRON, "vacuum_pump");
        setBlockBounds(BASE_BOUNDS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLL_BOUNDS;
    }

//    @Override
//    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, BlockPos pos) {
//        setBlockBounds(BBConstants.VACUUM_PUMP_MIN_POS, 0F, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MAX_POS, BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);
//    }

//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBounds(BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MAX_POS, BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVacuumPump.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.VACUUM_PUMP;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
