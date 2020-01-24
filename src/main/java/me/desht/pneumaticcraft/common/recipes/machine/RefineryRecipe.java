package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.recipes.MachineRecipeHandler;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RefineryRecipe implements IRefineryRecipe {
	public final FluidIngredient input;
	public final List<FluidStack> outputs;
	private ResourceLocation id;
	private final TemperatureRange operatingTemp;

	public RefineryRecipe(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs) {
		this.id = id;
		this.operatingTemp = operatingTemp;
		if (outputs.length < 2 || outputs.length > MAX_OUTPUTS) {
			throw new IllegalArgumentException("Recipe must have between 2 and " + MAX_OUTPUTS + " (inclusive) outputs");
		}
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
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public ResourceLocation getRecipeType() {
		return MachineRecipeHandler.Category.REFINERY.getId();
	}

	public static class Serializer extends AbstractRecipeSerializer<RefineryRecipe> {
        @Override
        public RefineryRecipe read(ResourceLocation recipeId, JsonObject json) {
        	Ingredient input = FluidIngredient.deserialize(json.get("input"));
        	int minTemp = JSONUtils.getInt(json, "min_temp", 373);
        	int maxTemp = JSONUtils.getInt(json, "max_temp", Integer.MAX_VALUE);
        	JsonArray outputs = json.get("results").getAsJsonArray();
        	if (outputs.size() < 2 || outputs.size() > IRefineryRecipe.MAX_OUTPUTS) {
        		throw new JsonSyntaxException("must be between 2 and 4 (inclusive) output fluids!");
			}
        	List<FluidStack> results = new ArrayList<>();
        	for (JsonElement element : outputs) {
        		results.add(ModCraftingHelper.fluidStackFromJSON(element.getAsJsonObject()));
			}
            return new RefineryRecipe(recipeId, (FluidIngredient) input,
					TemperatureRange.of(minTemp, maxTemp), results.toArray(new FluidStack[0]));
        }

        @Nullable
        @Override
        public RefineryRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidIngredient input = FluidIngredient.readFromPacket(buffer);
            TemperatureRange range = TemperatureRange.of(buffer.readVarInt(), buffer.readVarInt());
            int nOutputs = buffer.readVarInt();
            FluidStack[] outputs = new FluidStack[nOutputs];
            for (int i = 0; i < nOutputs; i++) {
                outputs[i] = FluidStack.readFromPacket(buffer);
            }
            return new RefineryRecipe(recipeId, input, range, outputs);
        }

        @Override
        public void write(PacketBuffer buffer, RefineryRecipe recipe) {
            super.write(buffer, recipe);

            recipe.input.writeToPacket(buffer);
            buffer.writeVarInt(recipe.operatingTemp.getMin());
            buffer.writeVarInt(recipe.operatingTemp.getMax());
            buffer.writeVarInt(recipe.outputs.size());
            recipe.outputs.forEach(f -> f.writeToPacket(buffer));
        }
    }
}
