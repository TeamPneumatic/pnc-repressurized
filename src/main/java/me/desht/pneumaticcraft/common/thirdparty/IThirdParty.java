package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public interface IThirdParty {
    /**
     * Called on both client and server after any registry objects are created, in the mod creation thread.
     */
    default void init() {}

    /**
     * Called on both client and server after any registry objects are created, on a scheduled tick (so in the main
     * execution thread).
     */
    default void postInit() {}

    /**
     * Called client-side after registry objects are created, in the mod creation thread.
     */
    default void clientInit() {}

    static void registerFuel(String fuelName, int mLperBucket) {
        registerFuel(new ResourceLocation(fuelName), mLperBucket);
    }

    static void registerFuel(ResourceLocation fuelName, int mLperBucket) {
        Fluid f = ForgeRegistries.FLUIDS.getValue(fuelName);
        if (f != null && f != Fluids.EMPTY) {
            PneumaticCraftAPIHandler.getInstance().getFuelRegistry().registerFuel(f, mLperBucket);
            Log.info("Registered " + fuelName + "' @ " + mLperBucket + " mL air/bucket");
        } else {
            Log.warning("Can't find fuel: " + fuelName);
        }
    }
}
