package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

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

    /**
     * Create a standard item lasering recipe.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a lasering recipe
     */
    static AssemblyRecipe basicLaserRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipe(id, input, output, AssemblyProgramType.LASER);
    }

    /**
     * Create a standard item drilling recipe.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a drilling recipe
     */
    static AssemblyRecipe basicDrillRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipe(id, input, output, AssemblyProgramType.DRILL);
    }

    enum AssemblyProgramType {
        DRILL, LASER, DRILL_LASER;

        public String getRegistryName() {
            return "assembly_program_" + this.toString().toLowerCase();
        }

    }
}
