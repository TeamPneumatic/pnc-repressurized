package me.desht.pneumaticcraft.common.thirdparty.enderio;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class EnderIO implements IThirdParty {

    @Override
    public void preInit() {
        registerFuel(Fluids.DIESEL);
        registerFuel(Fluids.KEROSENE);
        registerFuel(Fluids.GASOLINE);
        registerFuel(Fluids.LPG);
    }

    private void registerFuel(Fluid fluid) {
        registerFuel(fluid, 60, PneumaticCraftAPIHandler.getInstance().liquidFuels.get(fluid.getName()) / 60);
    }

    private void registerFuel(Fluid fluid, int powerPerCycle, int burnTime) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("fluidName", fluid.getName());
        tag.setInteger("powerPerCycle", powerPerCycle);
        tag.setInteger("totalBurnTime", burnTime);
        FMLInterModComms.sendMessage(ModIds.EIO, "fluidFuel:add", tag);
    }

    @Override
    public void init() {
        PneumaticRegistry.getInstance().registerXPLiquid(FluidRegistry.getFluid("xpjuice"), 20);
    }

    @Override
    public void postInit() {
        registerEnderIOFuel("hootch", 60 * 6000);
        registerEnderIOFuel("rocket_fuel", 160 * 7000);
        registerEnderIOFuel("fire_water", 80 * 15000);
    }

    private void registerEnderIOFuel(String fluidName, int value) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            Log.warning("Couldn't find EnderIO fuel fluid '" + fluidName + "'.  Has it been registered as a fuel?");
        } else {
            PneumaticRegistry.getInstance().registerFuel(fluid, value);
        }
    }
}
