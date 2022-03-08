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
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum FuelRegistry implements IFuelRegistry {
    INSTANCE;

    private static final FuelRecord MISSING_FUEL_ENTRY = new FuelRecord(0, 1f);

    // values which have been registered in code (could be accessed from off-thread via API)
    private final Map<Tag<Fluid>, FuelRecord> fuelTags = new ConcurrentHashMap<>();

    private final Map<Fluid, FuelRecord> cachedFuels = new HashMap<>();  // cleared on a /reload
    private final Map<Fluid, FuelRecord> hotFluids = new HashMap<>();

    public static FuelRegistry getInstance() {
        return INSTANCE;
    }

    // non-API!
    public void registerHotFluid(Fluid fluid, int mLPerBucket, float burnRateMultiplier) {
        hotFluids.put(fluid, new FuelRecord(mLPerBucket, burnRateMultiplier));
    }

    @Override
    public int getFuelValue(Level world, Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(world, fluid)).mLperBucket;
    }

    @Override
    public float getBurnRateMultiplier(Level world, Fluid fluid) {
        return cachedFuels.computeIfAbsent(fluid, k -> findEntry(world, fluid)).burnRateMultiplier;
    }

    @Override
    public Collection<Fluid> registeredFuels(Level world) {
        Set<Fluid> res = new HashSet<>(hotFluids.keySet());

        // recipes, from datapacks
        for (FuelQualityRecipe recipe : PneumaticCraftRecipeType.fuelQuality.getRecipes(world).values()) {
            res.addAll(recipe.getFuel().getFluidStacks().stream()
                    .map(FluidStack::getFluid)
                    .filter(f -> f.isSource(f.defaultFluidState()))
                    .toList());
        }

        // fluids tags added by code
        fuelTags.forEach((tag, entry) -> {
            if (entry.mLperBucket > 0) {
                List<Fluid> l = tag.getValues().stream().filter(f -> f.isSource(f.defaultFluidState())).toList();
                res.addAll(l);
            }
        });

        return res;
    }

    public void clearCachedFuelFluids() {
        // called when tags are reloaded
        cachedFuels.clear();
    }

    private FuelRecord findEntry(Level world, Fluid fluid) {
        // special case for high-temperature fluids
        FuelRecord fe = hotFluids.get(fluid);
        if (fe != null) return fe;

        // stuff from datapacks (override default registered stuff)
        for (FuelQualityRecipe recipe : PneumaticCraftRecipeType.fuelQuality.getRecipes(world).values()) {
            if (recipe.matchesFluid(fluid)) {
                return new FuelRecord(recipe.getAirPerBucket(), recipe.getBurnRate());
            }
        }

        // fluid tags registered in code
        for (Map.Entry<Tag<Fluid>, FuelRecord> entry : fuelTags.entrySet()) {
            if (entry.getKey().getValues().contains(fluid)) {
                return entry.getValue();
            }
        }

        return MISSING_FUEL_ENTRY;
    }

    private record FuelRecord(int mLperBucket, float burnRateMultiplier) {
    }
}
