package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.network.DescSynced;

public class TileEntityCreativeCompressor extends TileEntityPneumaticBase{
    @DescSynced
    public float pressureSetpoint;

    public TileEntityCreativeCompressor(){
        super(30, 30, 50000);
    }

    @Override
    public float getPressure(ForgeDirection sideRequested){
        return pressureSetpoint;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        pressureSetpoint = nbt.getFloat("setpoint");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        nbt.setFloat("setpoint", pressureSetpoint);
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote) {
            currentAir = (int)(getVolume() * pressureSetpoint);
        }
        super.updateEntity();
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        switch(guiID){
            case 0:
                pressureSetpoint -= 1;
                break;
            case 1:
                pressureSetpoint -= 0.1F;
                break;
            case 2:
                pressureSetpoint += 0.1F;
                break;
            case 3:
                pressureSetpoint += 1.0F;
                break;
        }
        if(pressureSetpoint > 30) pressureSetpoint = 30;
        if(pressureSetpoint < -1) pressureSetpoint = -1;
    }
}
