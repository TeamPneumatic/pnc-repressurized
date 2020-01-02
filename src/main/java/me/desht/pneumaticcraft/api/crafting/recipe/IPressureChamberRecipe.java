package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.crafting.StackedIngredient;
import me.desht.pneumaticcraft.common.recipes.machine.BasicPressureChamberRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IPressureChamberRecipe extends IModRecipe {
    NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();

    /**
     * Returns the minimum pressure required to craft the recipe. Negative pressures are also acceptable; in this
     * case the pressure chamber's pressure must be <strong>lower</strong> than the required pressure.
     *
     * @return threshold pressure
     */
    float getCraftingPressure();

    /**
     * Check if the given list of items is valid for this recipe.
     *
     * @param chamberHandler What's currently in the pressure chamber
     * @return true if this recipe is valid for what's in the chamber (note: pressure checks are not handled here)
     */
    boolean isValidRecipe(@Nonnull ItemStackHandler chamberHandler);

    /**
     * Get the input items for this recipe. This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    default List<Ingredient> getInputsForDisplay() {
        return Collections.emptyList();
    }

    /**
     * Get the output of this recipe, without crafting it.  This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    default NonNullList<ItemStack> getResultForDisplay() {
        return EMPTY_LIST;
    }

    /**
     * Check if the given item stack is a valid output item for this recipe.  This is used by the Pressure Chamber
     * Interface to decide if the item can be pulled from the chamber.
     *
     * @param stack the item stack to check
     * @return true if it's a valid output, false otherwise
     */
    boolean isOutputItem(ItemStack stack);

    /**
     * This method will be called when the recipe should output its items (after isValidRecipe returns true).  The
     * implementation is responsible for removing the items that have been used from the {@code chamberHandler}. The
     * implementation must also return the list of crafted items, for the Pressure Chamber to insert.
     *
     * @param chamberHandler items in the pressure chamber; should be modified to remove recipe input items.
     * @return the resulting items; these do not have to be copies - the Pressure Chamber itself will make sure they are copied
     */
    @Nonnull NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler chamberHandler);

    /**
     * Return a translation key for a supplementary tooltip to be displayed on the ingredient or resulting item.  For
     * use in recipe display systems such as JEI.
     *
     * @param input true if this is an input item, false if an output item
     * @param slot the slot number
     * @return a tooltip translation key, or "" for no tooltip
     */
    default String getTooltipKey(boolean input, int slot) {
        return "";
    }

    /**
     * Create a standard Pressure Chamber recipe. See also {@link StackedIngredient}, which may be helpful if you
     * want to add a recipe taking multiples of the same input item.
     *
     * @param id unique recipe ID
     * @param inputs a list of input ingredients
     * @param pressureRequired the pressure require (this is a minimum if positive, and a maximum if negative)
     * @param outputs the output item(s)
     * @return a recipe suitable for adding via {@link RegisterMachineRecipesEvent#getPressureChamber()}
     */
    static IPressureChamberRecipe basicRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        return new BasicPressureChamberRecipe(id, inputs, pressureRequired, outputs);
    }
}
