package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.api.tileentity.IManoMeasurable;

public class TileEntityPressureChamberWall extends TileEntityBase implements IManoMeasurable{

    protected TileEntityPressureChamberValve teValve;
    private int valveX;
    private int valveY;
    private int valveZ;

    public TileEntityPressureChamberWall(){}

    public TileEntityPressureChamberValve getCore(){
        if(teValve == null && (valveX != 0 || valveY != 0 || valveZ != 0)) {// when the saved TE equals null, check if we can
            // retrieve the TE from the NBT saved coords.

            TileEntity te = worldObj.getTileEntity(valveX, valveY, valveZ);
            setCore(te instanceof TileEntityPressureChamberValve ? (TileEntityPressureChamberValve)te : null);
        }
        return teValve;
    }

    public void onBlockBreak(){
        teValve = getCore();
        if(teValve != null) {
            teValve.onMultiBlockBreak();
        }
    }

    public void setCore(TileEntityPressureChamberValve te){

        if(te != null) {
            valveX = te.xCoord;
            valveY = te.yCoord;
            valveZ = te.zCoord;
        } else {
            valveX = 0;
            valveY = 0;
            valveZ = 0;
        }
        if(teValve != te) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        teValve = te;
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        valveX = tag.getInteger("valveX");
        valveY = tag.getInteger("valveY");
        valveZ = tag.getInteger("valveZ");
        teValve = null;

        if(worldObj != null && worldObj.isRemote) {
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("valveX", valveX);
        tag.setInteger("valveY", valveY);
        tag.setInteger("valveZ", valveZ);
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> curInfo){
        if(getCore() != null) {
            teValve.printManometerMessage(player, curInfo);
        }
    }

}
