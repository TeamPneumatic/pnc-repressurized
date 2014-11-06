package pneumaticCraft.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPneumaticDoor extends TileEntityBase{
    @DescSynced
    @LazySynced
    public float rotation;
    public float oldRotation;
    @DescSynced
    public boolean rightGoing;

    public void setRotation(float rotation){
        oldRotation = this.rotation;
        this.rotation = rotation;
        TileEntity te = null;
        if(getBlockMetadata() < 6) {
            te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
        } else {
            te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        }
        if(te instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor)te;
            door.rightGoing = rightGoing;
            if(rotation != door.rotation) {
                door.setRotation(rotation);
                //door.rotation = rotation;
                // door.oldRotation = oldRotation;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("rightGoing", rightGoing);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        rightGoing = tag.getBoolean("rightGoing");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }
}
