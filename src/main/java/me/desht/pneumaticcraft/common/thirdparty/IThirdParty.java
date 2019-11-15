package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

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

    static void registerFuel(String fuelName, int mLperBucket) {
        registerFuel(new ResourceLocation(fuelName), mLperBucket);
    }

    static void registerFuel(ResourceLocation fuelName, int mLperBucket) {
        Fluid f = ForgeRegistries.FLUIDS.getValue(fuelName);
        if (f != null && f != Fluids.EMPTY) {
            PneumaticCraftAPIHandler.getInstance().registerFuel(f, mLperBucket);
            Log.info("Registered " + fuelName + "' @ " + mLperBucket + " mL air/bucket");
        } else {
            Log.warning("Can't find fuel: " + fuelName);
        }
    }
}
