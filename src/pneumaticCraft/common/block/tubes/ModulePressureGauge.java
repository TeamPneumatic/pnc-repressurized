package pneumaticCraft.common.block.tubes;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.tubemodules.ModelGauge;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdatePressureBlock;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModulePressureGauge extends TubeModuleRedstoneEmitting{
    private final IBaseModel model = new ModelGauge(this);
    private float pressure;

    public ModulePressureGauge(){
        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public void update(){
        super.update();
        if(!pressureTube.world().isRemote) {
            if(pressureTube.world().getWorldTime() % 20 == 0) NetworkHandler.sendToAllAround(new PacketUpdatePressureBlock((TileEntityPneumaticBase)getTube()), getTube().world());
            setRedstone(getRedstone(pressureTube.getAirHandler().getPressure(ForgeDirection.UNKNOWN)));
        }
    }

    private int getRedstone(float pressure){
        return (int)((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @SideOnly(Side.CLIENT)
    public float getPressure(){
        return pressure;
    }

    @Override
    public String getType(){
        return Names.MODULE_GAUGE;
    }

    @Override
    public IBaseModel getModel(){
        return model;
    }

    @Override
    public double getWidth(){
        return 0.5;
    }

    @Override
    protected double getHeight(){
        return 0.25;
    }

    @Override
    public void addItemDescription(List<String> curInfo){
        curInfo.add(EnumChatFormatting.BLUE + "Formula: Redstone = 2.0 x pressure(bar)");
        curInfo.add("This module emits a redstone signal of which");
        curInfo.add("the strength is dependant on how much pressure");
        curInfo.add("the tube is at.");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        if(pressureTube != null && pressureTube.world() != null) {
            nbt.setFloat("pressure", pressureTube.getAirHandler().getPressure(ForgeDirection.UNKNOWN));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        pressure = nbt.getFloat("pressure");
    }

    @Override
    public boolean onActivated(EntityPlayer player){
        return super.onActivated(player);
    }

    @Override
    protected EnumGuiId getGuiId(){
        return EnumGuiId.PRESSURE_MODULE;
    }
}
