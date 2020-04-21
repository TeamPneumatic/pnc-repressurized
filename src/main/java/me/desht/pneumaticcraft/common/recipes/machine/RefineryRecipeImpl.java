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
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
	public void write(PacketBuffer buffer) {
		input.writeToPacket(buffer);
		buffer.writeVarInt(operatingTemp.getMin());
		buffer.writeVarInt(operatingTemp.getMax());
		buffer.writeVarInt(outputs.size());
		outputs.forEach(fluidStack -> fluidStack.writeToPacket(buffer));
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.REFINERY.get();
	}

	@Override
	public IRecipeType<?> getType() {
		return PneumaticCraftRecipeType.REFINERY;
	}

	@Override
	public String getGroup() {
		return ModBlocks.REFINERY.get().getRegistryName().getPath();
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(ModBlocks.REFINERY.get());
	}

	public static class Serializer<T extends RefineryRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
		private final IFactory<T> factory;

		public Serializer(IFactory<T> factory) {
			this.factory = factory;
		}

		@Override
        public T read(ResourceLocation recipeId, JsonObject json) {
        	Ingredient input = FluidIngredient.deserialize(json.get("input"));
        	TemperatureRange tempRange = TemperatureRange.fromJson(json.getAsJsonObject("temperature"));
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
        public T read(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidIngredient input = FluidIngredient.readFromPacket(buffer);
            TemperatureRange range = TemperatureRange.of(buffer.readVarInt(), buffer.readVarInt());
            int nOutputs = buffer.readVarInt();
            FluidStack[] outputs = new FluidStack[nOutputs];
            for (int i = 0; i < nOutputs; i++) {
                outputs[i] = FluidStack.readFromPacket(buffer);
            }
            return factory.create(recipeId, input, range, outputs);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe) {
        	recipe.write(buffer);
        }

        public interface IFactory<T extends RefineryRecipe> {
        	T create(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs);
		}
    }
}
