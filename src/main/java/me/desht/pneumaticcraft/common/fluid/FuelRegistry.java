package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public enum FuelRegistry implements IFuelRegistry {
    INSTANCE;

    private static final FuelEntry MISSING_FUEL_ENTRY = new FuelEntry(0, 1f);

    // could be accessed from off-thread via API
    private final Map<ITag<Fluid>, FuelEntry> liquidFuels = new ConcurrentHashMap<>();

    private final Map<Fluid, FuelEntry> cachedFuels = new HashMap<>();  // cleared on a /reload
    private final Map<Fluid, FuelEntry> hotFluids = new HashMap<>();

    public static FuelRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerFuel(ITag<Fluid> fluid, int mLPerBucket) {
        registerFuel(fluid, mLPerBucket, 1f);
    }

    @Override
    public void registerFuel(ITag<Fluid> fluidTag, int mLPerBucket, float burnRateMultiplier) {
        Validate.notNull(fluidTag);
        Validate.isTrue(mLPerBucket >= 0, "mlPerBucket can't be < 0!");
        Validate.isTrue(burnRateMultiplier > 0f, "burnRate can't be <= 0!");

        if (liquidFuels.containsKey(fluidTag)) {
            Log.warning("Overriding liquid fuel entry %s with a fuel value of %d (previous value %d)",
                    fluidTag, mLPerBucket, liquidFuels.get(fluidTag).mLperBucket);
            if (mLPerBucket == 0) {
                liquidFuels.remove(fluidTag);
            }
        }
        if (mLPerBucket > 0) {
            liquidFuels.put(fluidTag, new FuelEntry(mLPerBucket, burnRateMultiplier));
            Log.info("Registering liquid fuel entry '%s': %d mL air/bucket, burn rate %f",
                    fluidTag, mLPerBucket, burnRateMultiplier);
        }
    }

    // non-API!
    public void registerHotFluid(Fluid fluid, int mLPerBucket, float burnRateMultiplier) {
        hotFluids.put(fluid, new FuelEntry(mLPerBucket, burnRateMultiplier));
    }

    @Override
    public int getFuelValue(Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(fluid)).mLperBucket;
    }

    @Override
    public float getBurnRateMultiplier(Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(fluid)).burnRateMultiplier;
    }

    @Override
    public Collection<Fluid> registeredFuels() {
        List<Fluid> fluids = new ArrayList<>(hotFluids.keySet());
        liquidFuels.keySet().forEach(tag -> {
            List<Fluid> l = tag.getAllElements().stream().filter(f -> f.isSource(f.getDefaultState())).collect(Collectors.toList());
            fluids.addAll(l);
        });
        return fluids;
    }

    public void clearCachedFuelFluids() {
        // called when tags are reloaded
        cachedFuels.clear();
    }

    private FuelEntry findEntry(Fluid fluid) {
        // special case for high-temperature fluids
        FuelEntry fe = hotFluids.get(fluid);
        if (fe != null) return fe;

        for (Map.Entry<ITag<Fluid>, FuelEntry> entry : liquidFuels.entrySet()) {
            if (entry.getKey().contains(fluid)) {
                return entry.getValue();
            }
        }
        return MISSING_FUEL_ENTRY;
    }

    private static class FuelEntry {
        final int mLperBucket;
        final float burnRateMultiplier;

        private FuelEntry(int mLperBucket, float burnRateMultiplier) {
            this.mLperBucket = mLperBucket;
            this.burnRateMultiplier = burnRateMultiplier;
        }
    }
}
