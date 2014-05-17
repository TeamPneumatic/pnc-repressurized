package pneumaticExample;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.AirHandlerSupplier;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;

/**
 * Example Pneumatic TileEntity class.
 * Note that the Block#onNeigborChange also gets forwarded to the IAirHandler, this can be found in {link PneumaticExample}
 */
public class TileEntityPneumaticDiamond extends TileEntity implements IPneumaticMachine{
    private IAirHandler airHandler;

    @Override
    public IAirHandler getAirHandler(){
        if(airHandler == null) airHandler = AirHandlerSupplier.getAirHandler(5, 7, 50, 2000);
        return airHandler;
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side != ForgeDirection.UP;
    }

    @Override
    public void updateEntity(){
        //Remove air (on the server) when there's pressure. Note that you can pass any ForgeDirection if your TileEntity doesn't have multiple tanks.
        //Also note that to remove air, you can just call IAirHandler#addAir with a negative number. To add air simply call it with a positive number.
        if(!worldObj.isRemote && getAirHandler().getPressure(ForgeDirection.UNKNOWN) > 0) getAirHandler().addAir(-1, ForgeDirection.UNKNOWN);

        //Forward the method so dispersion, upgrade handling and explosions will be handled.
        getAirHandler().updateEntityI();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        getAirHandler().writeToNBTI(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        getAirHandler().readFromNBTI(tag);
    }

    @Override
    public void validate(){
        super.validate();
        getAirHandler().validateI(this);
    }

}
