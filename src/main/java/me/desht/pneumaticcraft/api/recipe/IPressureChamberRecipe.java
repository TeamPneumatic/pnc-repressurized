package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.ItemStack;

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
     * @return those stacks which will be used up (as opposed to simply modified); return null when the input stacks aren't valid for this recipe.
     */
    ItemStack[] isValidRecipe(ItemStack[] inputStacks);

    /**
     * When returned true, only the exact same references of the stacks returned by isValidRecipe() will be removed. This is useful
     * to remove stacks with a certain NBT value (like Enchanted Books). Return false for normal behaviour.
     *
     * @return true if exact stacks should be removed only.
     */
    boolean shouldRemoveExactStacks();

    /**
     * This method will be called when the recipe should output its items. the stacks the recipe output, may be dependent on the input stacks.
     *
     * @param inputStacks  These stacks can be modified (like adding/removing NBT data eg.)
     * @param removedStacks same reference to the stacks returned by isValidRecipe.
     * @return outputStacks. Stacks that will pop 'out of the chamber'
     */
    ItemStack[] craftRecipe(ItemStack[] inputStacks, ItemStack[] removedStacks);
}
