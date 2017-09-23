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
        Fluid hootch = FluidRegistry.getFluid("hootch");
        Fluid rocketFuel = FluidRegistry.getFluid("rocket_fuel");
        Fluid fireWater = FluidRegistry.getFluid("fire_water");

        if (hootch != null) {
            PneumaticRegistry.getInstance().registerFuel(hootch, 60 * 6000);
        } else {
            Log.warning("Couldn't find a fluid with name 'hootch' even though EnderIO is in the instance. It hasn't been registered as fuel!");
        }
        if (rocketFuel != null) {
            PneumaticRegistry.getInstance().registerFuel(rocketFuel, 160 * 7000);
        } else {
            Log.warning("Couldn't find a fluid with name 'rocket_fuel' even though EnderIO is in the instance. It hasn't been registered as fuel!");
        }
        if (fireWater != null) {
            PneumaticRegistry.getInstance().registerFuel(fireWater, 80 * 15000);
        } else {
            Log.warning("Couldn't find a fluid with name 'fire_water' even though EnderIO is in the instance. It hasn't been registered as fuel!");
        }
    }

    @Override
    public void clientSide() {

    }

    @Override
    public void clientInit() {

    }

}
