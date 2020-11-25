package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generic integration tasks which don't depend on specific mods' API.
 */
public class GenericIntegrationHandler implements IThirdParty {
    @Override
    public void postInit() {
        registerXPFluids();
        registerNonNativeFuels();
        ModdedWrenchUtils.getInstance().registerThirdPartyWrenches();
        ModNameCache.init();
    }

    private void registerNonNativeFuels() {
        // all done with fluid tags now, so we can be very mod-agnostic

        IFuelRegistry fuelApi = PneumaticCraftAPIHandler.getInstance().getFuelRegistry();

        // equivalent to LPG
        fuelApi.registerFuel(PneumaticCraftTags.Fluids.forgeTag("ethene"), 1800000, 1.25f);
        // low fuel value, but fast burning
        fuelApi.registerFuel(PneumaticCraftTags.Fluids.forgeTag("hydrogen"), 300000, 1.5f);
    }

    private void registerXPFluids() {
        // XP fluids are pretty mod-agnostic (just a fluid ID ResourceLocation).  So handle
        // XP fluid registration here rather than in mod-specific modules.

        maybeRegisterXPFluid(new ResourceLocation("industrialforegoing", "essence"), 20);
        maybeRegisterXPFluid(new ResourceLocation("cyclic", "xpjuice"), 20);

//        maybeRegisterXPFluid("experience", 20);  // CoFH Essence of Knowledge
    }

    @SuppressWarnings("SameParameterValue")
    private void maybeRegisterXPFluid(ResourceLocation fluidName, int xpRatio) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid != null && fluid != Fluids.EMPTY) {
            PneumaticRegistry.getInstance().registerXPFluid(fluid, xpRatio);
            Log.info("Registered experience fluid '" + fluidName + "' with mB->XP ratio " + xpRatio);
        } else {
            if (ModList.get().isLoaded(fluidName.getNamespace())) {
                Log.error("Attempted to register unknown experience fluid " + fluidName);
            }
        }
    }
}
