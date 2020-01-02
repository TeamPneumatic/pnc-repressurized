package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class RefineryRecipe implements IRefineryRecipe {
	public static final ResourceLocation RECIPE_TYPE = RL("refinery");

	public final FluidStack input;
	public final List<FluidStack> outputs;
	private ResourceLocation id;
	private final TemperatureRange operatingTemp;

	public RefineryRecipe(ResourceLocation id, FluidStack input, TemperatureRange operatingTemp, FluidStack... outputs) {
		this.id = id;
		this.operatingTemp = operatingTemp;
		if (outputs.length < 2 || outputs.length > MAX_OUTPUTS) {
			throw new IllegalArgumentException("Recipe must have between 2 and " + MAX_OUTPUTS + " (inclusive) outputs");
		}
		this.input = input;
		this.outputs = ImmutableList.copyOf(outputs);
	}

	@Override
	public FluidStack getInput() {
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
		return RECIPE_TYPE;
	}

	public static class Serializer extends AbstractRecipeSerializer<RefineryRecipe> {

        @Override
        public RefineryRecipe read(ResourceLocation recipeId, JsonObject json) {
            return null;
        }

        @Nullable
        @Override
        public RefineryRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            FluidStack input = FluidStack.readFromPacket(buffer);
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
