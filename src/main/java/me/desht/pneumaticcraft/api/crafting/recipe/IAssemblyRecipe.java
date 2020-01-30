package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public interface IAssemblyRecipe extends IModRecipe {
    /**
     * Get the input ingredient.
     * @return the input ingredient
     */
    Ingredient getInput();

    /**
     * Get the number of items required/used
     * @return the number of items
     */
    int getInputAmount();

    /**
     * Get the output item for this recipe.
     * @return the output item
     */
    @Nonnull
    ItemStack getOutput();

    /**
     * Get the program required.
     * @return the program type
     */
    AssemblyProgramType getProgramType();

    /**
     * Check if the given stack is a valid input for this recipe.
     * @param stack input stack
     * @return true if valid, false otherwise
     */
    boolean matches(ItemStack stack);

    enum AssemblyProgramType {
        DRILL, LASER, DRILL_LASER;

        public String getRegistryName() {
            return "assembly_program_" + this.toString().toLowerCase();
        }

    }
}
