package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public abstract class RefineryRecipe extends PneumaticCraftRecipe {
    public static final int MAX_OUTPUTS = 4;

    protected RefineryRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Get the input fluid ingredient for this recipe
     * @return the input fluid
     */
    public abstract FluidIngredient getInput();

    /**
     * Get a list of fluid outputs for this recipe
     * @return a list of output fluidstacks
     */
    public abstract List<FluidStack> getOutputs();

    /**
     * Get the valid operating temperature range for this recipe
     * @return the operating temperature range
     */
    public abstract TemperatureRange getOperatingTemp();
}
