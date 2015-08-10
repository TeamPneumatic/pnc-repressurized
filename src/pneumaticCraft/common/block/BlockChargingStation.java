package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockChargingStation extends BlockPneumaticCraftModeled{

    public BlockChargingStation(Material par2Material){
        super(par2Material);

    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
        setBlockBounds(BBConstants.CHARGING_STATION_MIN_POS, 0F, BBConstants.CHARGING_STATION_MIN_POS, BBConstants.CHARGING_STATION_MAX_POS, BBConstants.CHARGING_STATION_MAX_POS_TOP, BBConstants.CHARGING_STATION_MAX_POS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.CHARGING_STATION_MIN_POS, BBConstants.CHARGING_STATION_MIN_POS, BBConstants.CHARGING_STATION_MIN_POS, BBConstants.CHARGING_STATION_MAX_POS, BBConstants.CHARGING_STATION_MAX_POS_TOP, BBConstants.CHARGING_STATION_MAX_POS);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityChargingStation.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.CHARGING_STATION;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the
     * specified side. Args: World, X, Y, Z, side. Note that the side is
     * reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityChargingStation) {
            TileEntityChargingStation teCs = (TileEntityChargingStation)te;
            return teCs.shouldEmitRedstone() ? 15 : 0;
        }

        return 0;
    }

    @Override
    public boolean canProvidePower(){
        return true;
    }

    @Override
    protected int getInventoryDropEndSlot(IInventory inventory){
        return 5;
    }

}
