package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface IAssemblyRecipe extends IModRecipe {
    Item DRILL = ModItems.ASSEMBLY_PROGRAM_DRILL;
    Item LASER = ModItems.ASSEMBLY_PROGRAM_LASER;

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
     * @return an item for the program, which must be either {@link IAssemblyRecipe#DRILL} or
     * {@link IAssemblyRecipe#LASER}.  Anything else will probably cause a crash!
     */
    Item getProgram();

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
        return new AssemblyRecipe(id, input, output, ModItems.ASSEMBLY_PROGRAM_LASER);
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
        return new AssemblyRecipe(id, input, output, ModItems.ASSEMBLY_PROGRAM_DRILL);
    }
}
