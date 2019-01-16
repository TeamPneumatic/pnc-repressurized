package me.desht.pneumaticcraft.common.recipes;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefineryRecipe {

	public static final int MAX_OUTPUTS = 4;
	public static final List<RefineryRecipe> recipes = new ArrayList<>();
	
	public final FluidStack input;
	public final FluidStack[] outputs;
	private final int minimumTemp;

	public RefineryRecipe(int minimumTemp, FluidStack input, FluidStack[] outputs) {
		this.minimumTemp = minimumTemp;
		if (outputs.length < 2 || outputs.length > MAX_OUTPUTS) {
			throw new IllegalArgumentException("Recipe must have between 2 and " + MAX_OUTPUTS + " (inclusive) outputs");
		}
		this.input = input;
		this.outputs = outputs;
	}

	public int getMinimumTemp() {
		return minimumTemp;
	}

	public static Optional<RefineryRecipe> getRecipe(Fluid input, int size) {
		if (input == null || size <= 0) return Optional.empty();
		
		return recipes.stream()
				.filter(r -> r.outputs.length <= size && r.input.getFluid().equals(input))
				.min((r1, r2) -> Integer.compare(r2.outputs.length, r1.outputs.length));
	}
}
