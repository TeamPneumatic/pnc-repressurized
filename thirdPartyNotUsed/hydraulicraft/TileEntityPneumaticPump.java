package pneumaticCraft.common.thirdparty.hydraulicraft;

import k4unl.minecraft.Hydraulicraft.api.HydraulicBaseClassSupplier;
import k4unl.minecraft.Hydraulicraft.api.IBaseClass;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicGenerator;
import k4unl.minecraft.Hydraulicraft.api.PressureNetwork;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityPneumaticPump extends TileEntityPneumaticBase implements IHydraulicGenerator{
    private IBaseClass baseHandler;
    private PressureNetwork pNetwork;
    private int fluidInNetwork;
    private int networkCapacity;

    public TileEntityPneumaticPump(){
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_PUMP, PneumaticValues.MAX_PRESSURE_PNEUMATIC_PUMP, PneumaticValues.MAX_FLOW_PNEUMATIC_PUMP, PneumaticValues.VOLUME_PNEUMATIC_PUMP);
    }

    @Override
    public int getMaxStorage(){
        return 5000;//TODO determine value
    }

    @Override
    public float getMaxPressure(boolean isOil, ForgeDirection from){
        return 10000000;//Equal to tier 3 HC machines.
    }

    @Override
    public void onBlockBreaks(){} //An implementer could forward this method on his own if he wants to use it somewhere...

    @Override
    public IBaseClass getHandler(){
        if(baseHandler == null) baseHandler = HydraulicBaseClassSupplier.getBaseClass(this);
        return baseHandler;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound){}//Though I understand the system, it's a bit sophisticated to do it this way. Why not just overriding the 
                                                     //ordinary readFromNBT and writeToNBT, and forwarding to the IBaseClass? Simpler is better.

    @Override
    public void writeNBT(NBTTagCompound tagCompound){}

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        getHandler().writeToNBT(tag);
        if(pNetwork != null) {
            tag.setInteger("networkCapacity", getNetwork(ForgeDirection.UP).getFluidCapacity());
            tag.setInteger("fluidInNetwork", getNetwork(ForgeDirection.UP).getFluidInNetwork());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        getHandler().readFromNBT(tag);
    }

    @Override
    public void onPressureChanged(float old){}//Call hierarchy says it never gets called...

    @Override
    public void onFluidLevelChanged(int old){}

    @Override
    public boolean canConnectTo(ForgeDirection side){
        return true;
    }

    @Override
    public void firstTick(){}//Same deal, although nice, I think it would be better to make the implementer make a 'firstTick' call themselves if they need. Simpler the better (less methods).

    @Override
    public void updateNetwork(float oldPressure){//Copied straight from TileHydraulicPump... Needs to be better >.<.
        PressureNetwork newNetwork = null;
        PressureNetwork foundNetwork = null;
        PressureNetwork endNetwork = null;
        //This block can merge networks!
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            foundNetwork = PressureNetwork.getNetworkInDir(worldObj, xCoord, yCoord, zCoord, dir);
            if(foundNetwork != null) {
                if(endNetwork == null) {
                    endNetwork = foundNetwork;
                } else {
                    newNetwork = foundNetwork;
                }
                //  connectedSides.add(dir);
            }

            if(newNetwork != null && endNetwork != null) {
                //Hmm.. More networks!? What's this!?
                endNetwork.mergeNetwork(newNetwork);
                newNetwork = null;
            }
        }

        if(endNetwork != null) {
            pNetwork = endNetwork;
            pNetwork.addMachine(this, oldPressure, ForgeDirection.UP);
            //Log.info("Found an existing network (" + pNetwork.getRandomNumber() + ") @ " + xCoord + "," + yCoord + "," + zCoord);
        } else {
            pNetwork = new PressureNetwork(this, oldPressure, ForgeDirection.UP);
            //Log.info("Created a new network (" + pNetwork.getRandomNumber() + ") @ " + xCoord + "," + yCoord + "," + zCoord);
        }
    }

    @Override
    public PressureNetwork getNetwork(ForgeDirection side){
        return pNetwork;
    }

    @Override
    public void setNetwork(ForgeDirection side, PressureNetwork toSet){
        pNetwork = toSet;
    }

    @Override
    public void setPressure(float newPressure, ForgeDirection side){
        getNetwork(side).setPressure(newPressure);
    }

    @Override
    public int getFluidInNetwork(ForgeDirection from){
        if(worldObj.isRemote) {
            return fluidInNetwork;
        } else {
            return getNetwork(from).getFluidInNetwork();
        }
    }

    @Override
    public int getFluidCapacity(ForgeDirection from){
        if(worldObj.isRemote) {
            if(networkCapacity > 0) {
                return networkCapacity;
            } else {
                return getMaxStorage();
            }
        } else {
            return getNetwork(from).getFluidCapacity();
        }
    }

    @Override
    public void workFunction(ForgeDirection from){
        if(!worldObj.isRemote) {
            if(getPressure(ForgeDirection.UNKNOWN) > 0) {
                addAir(-10, ForgeDirection.UNKNOWN);
                //getHandler().
            }

        }
    }

    //FIXME getPressure(ForgeDirection) has same signature.

    @Override
    public boolean canWork(ForgeDirection dir){
        return dir.equals(ForgeDirection.UP);
    }

    @Override
    public int getMaxGenerating(ForgeDirection from){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getGenerating(ForgeDirection from){
        // TODO Auto-generated method stub
        return 0;
    }

}