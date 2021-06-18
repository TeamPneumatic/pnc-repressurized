package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import net.minecraft.fluid.Fluid;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CompoundFluidIngredient extends FluidIngredient {
    private final Stream<FluidIngredient> ingredients;
    private List<Fluid> fluids;
    private int dynamicAmount;

    CompoundFluidIngredient(Stream<FluidIngredient> ingredients) {
        super(null, 0, null, null);
        this.ingredients = ingredients;
    }

    @Override
    protected Collection<Fluid> getFluidList() {
        if (fluids == null) {
            Set<Fluid> f = ingredients.map(FluidIngredient::getFluidList).collect(HashSet::new, Set::addAll, Set::addAll);
            fluids = ImmutableList.copyOf(f);
            dynamicAmount = ingredients.map(FluidIngredient::getAmount).max(Integer::compare).orElse(0);
        }
        return fluids;
    }

    @Override
    public int getAmount() {
        return dynamicAmount;
    }
}
