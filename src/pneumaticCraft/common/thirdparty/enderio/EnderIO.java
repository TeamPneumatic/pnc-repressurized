package pneumaticCraft.common.thirdparty.enderio;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.event.FMLInterModComms;

public class EnderIO implements IThirdParty{

    @Override
    public void preInit(){
        registerFuel(Fluids.diesel);
        registerFuel(Fluids.kerosene);
        registerFuel(Fluids.gasoline);
        registerFuel(Fluids.lpg);

        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("hootch"), 60 * 6000);
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("rocket_fuel"), 160 * 7000);
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("fire_water"), 80 * 15000);
    }

    private void registerFuel(Fluid fluid){
        registerFuel(fluid, 60, PneumaticCraftAPIHandler.getInstance().liquidFuels.get(fluid.getName()) / 60);
    }

    private void registerFuel(Fluid fluid, int powerPerCycle, int burnTime){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("fluidName", fluid.getName());
        tag.setInteger("powerPerCycle", powerPerCycle);
        tag.setInteger("totalBurnTime", burnTime);
        FMLInterModComms.sendMessage(ModIds.EIO, "fluidFuel:add", tag);
    }

    @Override
    public void init(){

    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){

    }

    @Override
    public void clientInit(){

    }

}
