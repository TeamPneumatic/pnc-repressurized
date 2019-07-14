package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.config.Config;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidFuelManager {
    public static void registerFuels() {
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if (fluid.getTemperature() > Config.Common.General.minFluidFuelTemperature) {
                PneumaticRegistry.getInstance().registerFuel(fluid, (fluid.getTemperature() - 300) * 40);
            }
        }
    }
}
