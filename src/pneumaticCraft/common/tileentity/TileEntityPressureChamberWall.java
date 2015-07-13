package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.api.tileentity.IManoMeasurable;
import pneumaticCraft.common.network.DescSynced;

public class TileEntityPressureChamberWall extends TileEntityBase implements IManoMeasurable{

    protected TileEntityPressureChamberValve teValve;
    @DescSynced
    private int valveX;
    @DescSynced
    private int valveY;
    @DescSynced
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

        if(!worldObj.isRemote) {
            if(te != null) {
                valveX = te.xCoord;
                valveY = te.yCoord;
                valveZ = te.zCoord;
            } else {
                valveX = 0;
                valveY = 0;
                valveZ = 0;
            }
        }
        teValve = te;
    }

    @Override
    public void onDescUpdate(){
        super.onDescUpdate();
        teValve = null;
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

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate(){
        return true;
    }

}
