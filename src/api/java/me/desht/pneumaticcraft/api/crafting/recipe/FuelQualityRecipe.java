package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

public abstract class FuelQualityRecipe extends PneumaticCraftRecipe {
    protected FuelQualityRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Does the given fluid match this recipe?
     * @param inputFluid the fluid to test
     * @return true if it matches, false otherwise
     */
    public abstract boolean matchesFluid(Fluid inputFluid);

    /**
     * Get the fuel for this recipe
     * @return the fuel
     */
    public abstract FluidIngredient getFuel();

    /**
     * Get the amount of compressed air (in mL) produced by burning 1000mB of this fuel in a liquid compressor,
     * with no speed upgrades.
     * @return the amount of air produced by this fuel fluid
     */
    public abstract int getAirPerBucket();

    /**
     * Get the burn rate for this fuel. Burn rate affects the speed at which the fuel is consumed (and compressed
     * air produced), without affecting the overall quantity of air produced.
     * @return
     */
    public abstract float getBurnRate();
}
