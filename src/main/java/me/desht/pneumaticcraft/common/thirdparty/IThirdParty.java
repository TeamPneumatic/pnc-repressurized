package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public interface IThirdParty {

    default void preInit() {}

    default void init() {}

    default void postInit() {}

    /**
     * Gets called from the ClientProxy in the preInit.
     */
    default void clientPreInit() {}

    /**
     * Gets called from the ClientProxy in the Init.
     */
    default void clientInit() {}

    static void registerFuel(String fuelName, String modName, int mLperBucket) {
        Fluid f = FluidRegistry.getFluid(fuelName);
        if (f != null) {
            PneumaticCraftAPIHandler.getInstance().registerFuel(f, mLperBucket);
            PneumaticCraftRepressurized.logger.info("Registered " + modName + " fuel '" + fuelName + "' @ " + mLperBucket + " mL air/bucket");
        } else {
            PneumaticCraftRepressurized.logger.warn("Can't find " + modName + " fuel: " + fuelName);
        }
    }
}
