package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum FuelRegistry implements IFuelRegistry {
    INSTANCE;

    private static final Pair<Integer, Float> MISSING = Pair.of(0, 1f);

    public final Map<ResourceLocation, Pair<Integer, Float>> liquidFuels = new HashMap<>();

    public static FuelRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerFuel(Fluid fluid, int mLPerBucket) {
        registerFuel(fluid, mLPerBucket, 1f);
    }

    @Override
    public void registerFuel(Fluid fluid, int mLPerBucket, float burnRateMultiplier) {
        Validate.notNull(fluid);
        Validate.isTrue(mLPerBucket >= 0, "mlPerBucket can't be < 0!");
        Validate.isTrue(burnRateMultiplier > 0f, "burnRate can't be <= 0!");

        if (liquidFuels.containsKey(fluid.getRegistryName())) {
            Log.warning("Overriding liquid fuel entry %s (%s) with a fuel value of %d (previous value %d)",
                    new FluidStack(fluid, 1).getDisplayName().getString(),
                    fluid.getRegistryName().toString(), mLPerBucket, liquidFuels.get(fluid.getRegistryName()));
            if (mLPerBucket == 0) {
                liquidFuels.remove(fluid.getRegistryName());
            }
        }
        if (mLPerBucket > 0) {
            liquidFuels.put(fluid.getRegistryName(), Pair.of(mLPerBucket, burnRateMultiplier));
            Log.info("Registering liquid fuel entry '%s': %d mL air/bucket, burn rate %f",
                    fluid.getRegistryName(), mLPerBucket, burnRateMultiplier);
        }

    }

    @Override
    public int getFuelValue(Fluid fluid) {
        return liquidFuels.getOrDefault(fluid.getRegistryName(), MISSING).getLeft();
    }

    @Override
    public float getBurnRateMultiplier(Fluid fluid) {
        return liquidFuels.getOrDefault(fluid.getRegistryName(), MISSING).getRight();
    }

    @Override
    public Collection<Fluid> registeredFuels() {
        return liquidFuels.keySet().stream().map(ForgeRegistries.FLUIDS::getValue).collect(Collectors.toList());
    }
}
