package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidSetup {
    /**
     * Fluid setup tasks to be done AFTER fluids (and items/blocks) are registered
     */
    public static void init() {
        PneumaticCraftAPIHandler api = PneumaticCraftAPIHandler.getInstance();
        IFuelRegistry fuelApi = api.getFuelRegistry();

        // proper fuels
        fuelApi.registerFuel(ModFluids.OIL.get(), 200000, 0.25f);
        fuelApi.registerFuel(ModFluids.DIESEL.get(), 1000000, 0.8f);
        fuelApi.registerFuel(ModFluids.KEROSENE.get(), 1100000);
        fuelApi.registerFuel(ModFluids.GASOLINE.get(), 1500000, 1.5f);
        fuelApi.registerFuel(ModFluids.LPG.get(), 1800000, 1.25f);

        // register hot fluids as (fairly inefficient) fuels
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid.getAttributes().getTemperature() >= PNCConfig.Common.General.minFluidFuelTemperature && fluid.isSource(fluid.getDefaultState())) {
                fuelApi.registerFuel(fluid, (fluid.getAttributes().getTemperature() - 300) * 40, 0.25f);
            }
        }

        // no magnet'ing PCB's out of etching acid pools
        api.getItemRegistry().registerMagnetSuppressor(
                e -> e instanceof ItemEntity && ((ItemEntity) e).getItem().getItem() == ModItems.EMPTY_PCB.get()
                        && e.getEntityWorld().getFluidState(e.getPosition()).getFluid() == ModFluids.ETCHING_ACID.get()
        );

        api.registerXPFluid(ModFluids.MEMORY_ESSENCE.get(), 20);
    }
}
