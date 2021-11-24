/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        super(null, 0, null, null, null, false);
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
