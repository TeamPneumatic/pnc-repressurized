package me.desht.pneumaticcraft.api.recipe;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public interface IPressureChamberRecipe {

    /**
     * Returns the threshold which is minimal to craft the recipe. Negative pressures are also acceptable; in this
     * case the pressure chamber's pressure must be <strong>lower</strong> than the required pressure.
     *
     * @return threshold pressure
     */
    float getCraftingPressure();

    /**
     * Check if the given list of items is valid for this recipe.
     *
     * @param inputStacks the items to provide as input; what's currently in the pressure chamber
     * @return true if a valid recipe
     */
    boolean isValidRecipe(@Nonnull ItemStackHandler inputStacks);

    /**
     * This method will be called when the recipe should output its items (after isValidRecipe returns true).
     * the items used need to be removed from the inputStacks ItemStackHandler. The output stacks need to be returned (as theoretically the ItemStackHandler can fill up)
     *
     * @param inputStacks  These stacks can be modified, to remove the items used, for example.
     * @return outputStacks. The recipe result. These do not have to be copies, the Pressure Chamber will make sure they will be copied.
     */
    @Nonnull NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler inputStacks);
}
