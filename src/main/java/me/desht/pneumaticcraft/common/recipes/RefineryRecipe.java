package me.desht.pneumaticcraft.common.recipes;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefineryRecipe {
	
	public static List<RefineryRecipe> recipes = new ArrayList<>();
	
	public final FluidStack input;
	public final FluidStack[] outputs;
	
	public RefineryRecipe(FluidStack input, FluidStack[] outputs) {
		if (outputs.length < 2 || outputs.length > 4) {
			throw new IllegalArgumentException("It must be at least 2 outputs present and ");
		}
		this.input = input;
		this.outputs = outputs;
	}
	
	public static Optional<RefineryRecipe> getRecipe(Fluid input, int size) {
		if (input == null || size <= 0) return Optional.empty();
		
		return recipes.stream().filter(r -> r.outputs.length <= size && r.input.getFluid().equals(input)).sorted((r1, r2) -> Integer.compare(r2.outputs.length, r1.outputs.length)).findFirst();
	}
}
