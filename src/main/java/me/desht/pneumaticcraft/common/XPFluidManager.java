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

package me.desht.pneumaticcraft.common;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.core.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public enum XPFluidManager {
    INSTANCE;

    private final Map<Fluid, Integer> liquidXPs = new HashMap<>();
    private List<Fluid> availableLiquidXPs = null; // for cycling through xp fluid types
    private final List<Pair<FluidIngredient, Integer>> pendingIngredients = new ArrayList<>();

    public static XPFluidManager getInstance() {
        return INSTANCE;
    }

    public void registerXPFluid(Fluid fluid, int liquidToPointRatio) {
        Validate.isTrue(fluid != null && fluid != Fluids.EMPTY, "Fluid may not be null!");
        if (liquidToPointRatio <= 0) {
            liquidXPs.remove(fluid);
        } else {
            liquidXPs.put(fluid, liquidToPointRatio);
        }
        availableLiquidXPs = null;  // force recalc on next query
    }

    public void registerXPFluid(FluidIngredient fluidIngredient, int liquidToPointRatio) {
        pendingIngredients.add(Pair.of(fluidIngredient, liquidToPointRatio));
    }

    public int getXPRatio(Fluid fluid) {
        if (!pendingIngredients.isEmpty()) {
            resolveFluidIngredients();
            pendingIngredients.clear();
        }
        return liquidXPs.getOrDefault(fluid, 0);
    }

    private void resolveFluidIngredients() {
        for (Pair<FluidIngredient, Integer> pair : pendingIngredients) {
            for (FluidStack fluidStack: pair.getLeft().getFluidStacks()) {
                Fluid fluid = fluidStack.getFluid();
                if (fluid.isSource(fluid.defaultFluidState()) && fluidStack.getAmount() > 0) {
                    registerXPFluid(fluid, pair.getRight() / fluidStack.getAmount());
                }
            }
        }
    }

    public List<Fluid> getAvailableLiquidXPs() {
        if (availableLiquidXPs == null) {
            // little kludge: ensure our own Memory Essence is always first in the list
            Set<Fluid> tmpSet = new HashSet<>(liquidXPs.keySet());
            ImmutableList.Builder<Fluid> builder = ImmutableList.builder();
            if (tmpSet.remove(ModFluids.MEMORY_ESSENCE.get())) {
                builder.add(ModFluids.MEMORY_ESSENCE.get());
            }
            builder.addAll(tmpSet);
            availableLiquidXPs = builder.build();
        }
        return availableLiquidXPs;
    }
}
