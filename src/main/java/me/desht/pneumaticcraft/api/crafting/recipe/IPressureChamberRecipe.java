package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
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
     * @param chamberHandler what's currently in the pressure chamber
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
     * This method will be called when the recipe should output its items, and will only be called when
     * {@link #isValidRecipe(ItemStackHandler)} returns true, i.e. the necessary items are definitely in the chamber.
     * The implementation is responsible for removing the items that have been used from the {@code chamberHandler}. The
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
}
