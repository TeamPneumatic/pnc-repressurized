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

package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RefineryRecipeImpl extends RefineryRecipe {
	public final FluidIngredient input;
	public final List<FluidStack> outputs;
	private final TemperatureRange operatingTemp;

	public RefineryRecipeImpl(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs) {
		super(id);

		this.operatingTemp = operatingTemp;
		Validate.isTrue(outputs.length >= 2 && outputs.length <= MAX_OUTPUTS,
				"Recipe must have between 2 and " + MAX_OUTPUTS + " (inclusive) outputs");
		this.input = input;
		this.outputs = ImmutableList.copyOf(outputs);
	}

	@Override
	public FluidIngredient getInput() {
		return input;
	}

	@Override
	public List<FluidStack> getOutputs() {
		return outputs;
	}

	@Override
	public TemperatureRange getOperatingTemp() {
		return operatingTemp;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		input.toNetwork(buffer);
		operatingTemp.write(buffer);
		buffer.writeVarInt(outputs.size());
		outputs.forEach(fluidStack -> fluidStack.writeToPacket(buffer));
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.REFINERY.get();
	}

	@Override
	public RecipeType<?> getType() {
		return PneumaticCraftRecipeType.REFINERY;
	}

	@Override
	public String getGroup() {
		return ModBlocks.REFINERY.get().getRegistryName().getPath();
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(ModBlocks.REFINERY.get());
	}

	public static class Serializer<T extends RefineryRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
		private final IFactory<T> factory;

		public Serializer(IFactory<T> factory) {
			this.factory = factory;
		}

		@Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
        	Ingredient input = FluidIngredient.fromJson(json.get("input"));
        	TemperatureRange tempRange;
        	if (json.has("temperature")) {
				tempRange = TemperatureRange.fromJson(json.getAsJsonObject("temperature"));
			} else {
        		tempRange = TemperatureRange.min(373);
			}
        	JsonArray outputs = json.get("results").getAsJsonArray();
        	if (outputs.size() < 2 || outputs.size() > RefineryRecipe.MAX_OUTPUTS) {
        		throw new JsonSyntaxException("must be between 2 and 4 (inclusive) output fluids!");
			}
        	List<FluidStack> results = new ArrayList<>();
        	for (JsonElement element : outputs) {
        		results.add(ModCraftingHelper.fluidStackFromJson(element.getAsJsonObject()));
			}
            return factory.create(recipeId, (FluidIngredient) input, tempRange, results.toArray(new FluidStack[0]));
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            FluidIngredient input = (FluidIngredient) Ingredient.fromNetwork(buffer);
            TemperatureRange range = TemperatureRange.read(buffer);
            int nOutputs = buffer.readVarInt();
            FluidStack[] outputs = new FluidStack[nOutputs];
            for (int i = 0; i < nOutputs; i++) {
                outputs[i] = FluidStack.readFromPacket(buffer);
            }
            return factory.create(recipeId, input, range, outputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        	recipe.write(buffer);
        }

        public interface IFactory<T extends RefineryRecipe> {
        	T create(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs);
		}
    }
}
