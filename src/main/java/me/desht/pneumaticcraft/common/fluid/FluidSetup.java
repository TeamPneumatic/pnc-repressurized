package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.entity.item.ItemEntity;

public class FluidSetup {
    /**
     * Fluid setup tasks to be done AFTER fluids (and items/blocks) are registered
     */
    public static void init() {
        PneumaticRegistry.getInstance().registerFuel(ModFluids.OIL.get(), 64000);
        PneumaticRegistry.getInstance().registerFuel(ModFluids.DIESEL.get(), 700000);
        PneumaticRegistry.getInstance().registerFuel(ModFluids.KEROSENE.get(), 1100000);
        PneumaticRegistry.getInstance().registerFuel(ModFluids.GASOLINE.get(), 1500000);
        PneumaticRegistry.getInstance().registerFuel(ModFluids.LPG.get(), 1800000);

        // no magnet'ing PCB's out of etching acid pools
        PneumaticCraftAPIHandler.getInstance().getItemRegistry().registerMagnetSuppressor(
                e -> e instanceof ItemEntity && ((ItemEntity) e).getItem().getItem() == ModItems.EMPTY_PCB.get()
                        && e.getEntityWorld().getFluidState(e.getPosition()).getFluid() == ModFluids.ETCHING_ACID.get()
        );
    }
}
