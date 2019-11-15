package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generic integration tasks which don't depend on specific mods' API.
 */
public class GenericIntegrationHandler implements IThirdParty {
    @Override
    public void postInit() {
        registerXPFluids();
        ModdedWrenchUtils.getInstance().registerThirdPartyWrenches();
        ModNameCache.init();
    }

    private void registerXPFluids() {
        // XP fluids are pretty mod-agnostic (just a String fluid name), and the same fluid can be registered
        // by multiple mods ("xpjuice" is registered by EnderIO, OpenBlocks and Cyclic for example).  So handle
        // XP fluid registration here rather than in mod-specific modules.

        // TODO 1.14 probably need to be using fluid tags here....

//        maybeRegisterXPFluid("xpjuice", 20);  // XP Juice from EnderIO, Cyclic, OpenBlocks, others?
//        maybeRegisterXPFluid("mobessence", 77); // MFR Mob Essence (not in 1.12.2 at this time)
//        maybeRegisterXPFluid("essence", 20);  // Industrial Foregoing Essence
//        maybeRegisterXPFluid("experience", 20);  // CoFH Essence of Knowledge
    }

    private void maybeRegisterXPFluid(ResourceLocation fluidName, int xpRatio) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid != null) {
            PneumaticRegistry.getInstance().registerXPFluid(fluid, xpRatio);
            Log.info("Registered experience fluid '" + fluidName + "' with mB->XP ratio " + xpRatio);
        }
    }
}
