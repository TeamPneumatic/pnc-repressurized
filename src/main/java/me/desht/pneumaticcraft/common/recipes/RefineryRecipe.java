package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.api.recipe.TemperatureRange;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class RefineryRecipe implements IRefineryRecipe {

	public static final int MAX_OUTPUTS = 4;
	public static final List<IRefineryRecipe> recipes = new ArrayList<>();
	
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
	public void write(PacketBuffer buf) {
		buf.writeResourceLocation(id);
		input.writeToPacket(buf);
		buf.writeVarInt(operatingTemp.getMin());
		buf.writeVarInt(operatingTemp.getMax());
		buf.writeVarInt(outputs.size());
		outputs.forEach(f -> f.writeToPacket(buf));
	}
}
