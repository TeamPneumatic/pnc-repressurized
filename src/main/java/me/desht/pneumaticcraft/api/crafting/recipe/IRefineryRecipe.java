package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IRefineryRecipe extends IModRecipe {
    int MAX_OUTPUTS = 4;

    FluidIngredient getInput();

    List<FluidStack> getOutputs();

    TemperatureRange getOperatingTemp();
}
