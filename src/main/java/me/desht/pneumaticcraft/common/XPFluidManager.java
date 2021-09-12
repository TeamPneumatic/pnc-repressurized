package me.desht.pneumaticcraft.common;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.core.ModFluids;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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
