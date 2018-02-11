package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class XPFluids {
    static void registerXPFluids() {
        // XP fluids are pretty mod-agnostic (just a String fluid name), and the same fluid can be registered
        // by multiple mods ("xpjuice" is registered by EnderIO, OpenBlocks and Cyclic for example).  So handle
        // XP fluid registration here rather than in mod-specific modules.

        maybeRegisterXPFluid("xpjuice", 20);  // XP Juice from EnderIO, Cyclic, OpenBlocks, others?
        maybeRegisterXPFluid("mobessence", 77); // MFR Mob Essence (not in 1.12.2 at this time)
        maybeRegisterXPFluid("essence", 20);  // Industrial Foregoing Essence
        maybeRegisterXPFluid("experience", 20);  // CoFH Essence of Knowledge
    }

    private static void maybeRegisterXPFluid(String fluidName, int xpRatio) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid != null) {
            PneumaticRegistry.getInstance().registerXPLiquid(fluid, xpRatio);
            String modId = FluidRegistry.getModId(new FluidStack(fluid, 1000));
            Log.info("Registered experience fluid '" + fluidName + "' from mod '" + modId + "' with mB->XP ratio " + xpRatio);
        }
    }
}
