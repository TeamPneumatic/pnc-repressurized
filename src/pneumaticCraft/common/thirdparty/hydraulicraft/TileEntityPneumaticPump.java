package pneumaticCraft.common.thirdparty.hydraulicraft;

import k4unl.minecraft.Hydraulicraft.api.HydraulicBaseClassSupplier;
import k4unl.minecraft.Hydraulicraft.api.IBaseClass;
import k4unl.minecraft.Hydraulicraft.api.IHydraulicGenerator;
import k4unl.minecraft.Hydraulicraft.api.PressureTier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityPneumaticPump extends TileEntityPneumaticBase implements IHydraulicGenerator{
    private final IBaseClass handler = HydraulicBaseClassSupplier.getBaseClass(this, PressureTier.HIGHPRESSURE, 10);

    public TileEntityPneumaticPump(){
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_PUMP, PneumaticValues.MAX_PRESSURE_PNEUMATIC_PUMP, PneumaticValues.VOLUME_PNEUMATIC_PUMP);
    }

    @Override
    public IBaseClass getHandler(){
        return handler;
    }

    @Override
    public void onFluidLevelChanged(int old){}

    @Override
    public boolean canConnectTo(ForgeDirection side){
        return true;
    }

    @Override
    public void workFunction(ForgeDirection from){
        int airUsed = (int)(getPressure(from) * 10);
        handler.addPressureWithRatio(150 * airUsed * Config.pneumaticPumpEfficiency / 100, from);
        addAir(-airUsed, from);
    }

    @Override
    public boolean canWork(ForgeDirection dir){
        return getPressure(dir) > PneumaticValues.MIN_PRESSURE_PNEUMATIC_PUMP && ForgeDirection.UP == dir;
    }

    @Override
    public int getMaxGenerating(ForgeDirection from){
        return 100;
    }

    @Override
    public float getGenerating(ForgeDirection from){
        return 100;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        handler.readFromNBTI(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        handler.writeToNBTI(tag);
    }

    @Override
    public void validate(){
        handler.init(this);
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        handler.updateEntityI();
    }

}