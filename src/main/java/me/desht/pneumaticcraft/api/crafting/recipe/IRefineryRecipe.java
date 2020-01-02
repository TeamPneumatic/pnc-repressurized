package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.common.recipes.machine.RefineryRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IRefineryRecipe extends IModRecipe {
    int MAX_OUTPUTS = 4;

    FluidStack getInput();

    List<FluidStack> getOutputs();

    TemperatureRange getOperatingTemp();

    /**
     * Create a standard Refinery recipe.  Note that multiple recipes with the same input fluid may exist, provided that
     * each recipe has a different number of output fluids; the Refinery will use the recipe with the largest number
     * of outputs, limited by the number of output tanks in the Refinery multiblock.
     *
     * @param id unique ID for this recipe
     * @param input the input fluid
     * @param operatingTemp a temperature range required for the recipe to craft
     * @param outputs the output fluids
     * @return a basic Refinery recipe
     */
    static IRefineryRecipe basicRecipe(ResourceLocation id, FluidStack input, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipe(id, input, operatingTemp, outputs);
    }
}
