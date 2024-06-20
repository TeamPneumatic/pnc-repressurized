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

package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.common.recipes.other.FuelQualityRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class FuelQualityBuilder extends AbstractPNCRecipeBuilder {
    private final FluidIngredient fuel;
    private final int airPerBucket;
    private final float burnRate;

    public FuelQualityBuilder(FluidIngredient fuel, int airPerBucket, float burnRate) {
        this.fuel = fuel;
        this.airPerBucket = airPerBucket;
        this.burnRate = burnRate;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new FuelQualityRecipeImpl(fuel, airPerBucket, burnRate), null);
    }
}
