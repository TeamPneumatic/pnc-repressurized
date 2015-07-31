package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockVacuumPump extends BlockPneumaticCraftModeled{

    public BlockVacuumPump(Material par2Material){
        super(par2Material);

    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
        setBlockBounds(BBConstants.VACUUM_PUMP_MIN_POS, 0F, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MAX_POS, BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MIN_POS, BBConstants.VACUUM_PUMP_MAX_POS, BBConstants.VACUUM_PUMP_MAX_POS_TOP, BBConstants.VACUUM_PUMP_MAX_POS);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityVacuumPump.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.VACUUM_PUMP;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
