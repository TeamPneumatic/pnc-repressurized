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

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class FluidMixerRecipeBuilder extends PneumaticCraftRecipeBuilder<FluidMixerRecipeBuilder> {
    private final FluidIngredient input1;
    private final FluidIngredient input2;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final float pressure;
    private final int processingTime;

    public FluidMixerRecipeBuilder(FluidIngredient input1, FluidIngredient input2, FluidStack outputFluid, ItemStack outputItem, float pressure, int processingTime) {
        super(RL(PneumaticCraftRecipeTypes.FLUID_MIXER));

        this.input1 = input1;
        this.input2 = input2;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.pressure = pressure;
        this.processingTime = processingTime;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new FluidMixerRecipeResult(id);
    }

    public class FluidMixerRecipeResult extends RecipeResult {
        public FluidMixerRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input1", input1.toJson());
            json.add("input2", input2.toJson());
            if (!outputFluid.isEmpty()) json.add("fluid_output", ModCraftingHelper.fluidStackToJson(outputFluid));
            if (!outputItem.isEmpty()) json.add("item_output", SerializerHelper.serializeOneItemStack(outputItem));
            json.addProperty("pressure", pressure);
            if (processingTime != 200) json.addProperty("time", processingTime);
        }
    }
}
