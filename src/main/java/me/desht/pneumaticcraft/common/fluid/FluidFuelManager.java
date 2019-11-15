package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidFuelManager {
    public static void registerFuels() {
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid.getAttributes().getTemperature() > PNCConfig.Common.General.minFluidFuelTemperature) {
                PneumaticRegistry.getInstance().registerFuel(fluid, (fluid.getAttributes().getTemperature() - 300) * 40);
            }
        }
    }
}
