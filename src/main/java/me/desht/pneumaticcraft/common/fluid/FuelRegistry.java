/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum FuelRegistry implements IFuelRegistry {
    INSTANCE;

    private static final FuelRecord MISSING_FUEL_ENTRY = new FuelRecord(0, 1f);

    private final Map<Fluid, FuelRecord> cachedFuels = new ConcurrentHashMap<>();  // cleared on a /reload
    private final Map<Fluid, FuelRecord> hotFluids = new ConcurrentHashMap<>();

    public static FuelRegistry getInstance() {
        return INSTANCE;
    }

    // non-API!
    public void registerHotFluid(Fluid fluid, int mLPerBucket, float burnRateMultiplier) {
        hotFluids.put(fluid, new FuelRecord(mLPerBucket, burnRateMultiplier));
    }

    @Override
    public int getFuelValue(Level level, Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(level, fluid)).mLperBucket;
    }

    @Override
    public float getBurnRateMultiplier(Level world, Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(world, fluid)).burnRateMultiplier;
    }

    @Override
    public Collection<Fluid> registeredFuels(Level level) {
        Set<Fluid> res = new HashSet<>(hotFluids.keySet());

        for (FuelQualityRecipe recipe : ModRecipeTypes.FUEL_QUALITY.get().allRecipes(level)) {
            res.addAll(recipe.getFuel().getFluidStacks().stream()
                    .map(FluidStack::getFluid)
                    .filter(f -> f.isSource(f.defaultFluidState()))
                    .toList());
        }

        return res;
    }

    public void clearCachedFuelFluids() {
        // called when tags are reloaded
        cachedFuels.clear();
    }

    private FuelRecord findEntry(Level level, Fluid fluid) {
        // special case for high-temperature fluids
        FuelRecord fe = hotFluids.get(fluid);
        if (fe != null) return fe;

        // stuff from datapacks (override default registered stuff)
        for (FuelQualityRecipe recipe : ModRecipeTypes.FUEL_QUALITY.get().allRecipes(level)) {
            if (recipe.matchesFluid(fluid)) {
                return new FuelRecord(recipe.getAirPerBucket(), recipe.getBurnRate());
            }
        }

        return MISSING_FUEL_ENTRY;
    }

    private record FuelRecord(int mLperBucket, float burnRateMultiplier) {
    }
}
