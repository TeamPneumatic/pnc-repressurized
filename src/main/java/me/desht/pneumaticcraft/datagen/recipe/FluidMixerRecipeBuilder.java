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

import me.desht.pneumaticcraft.common.recipes.machine.FluidMixerRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class FluidMixerRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final SizedFluidIngredient input1;
    private final SizedFluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeBuilder(SizedFluidIngredient input1, SizedFluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new FluidMixerRecipeImpl(input1, input2, outputFluid, outputItem, pressure, processingTime), null);
    }
}
