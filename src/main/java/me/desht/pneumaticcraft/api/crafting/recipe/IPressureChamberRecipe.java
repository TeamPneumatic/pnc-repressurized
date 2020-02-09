package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Collection;
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
     * Try to find the ingredients for this recipe in the given item handler, which is the items currently in the
     * pressure chamber. Returns a collection of slot indexes into the item handler, which should be passed promptly to
     * {@link #craftRecipe(IItemHandlerModifiable, List)}  and not cached across ticks, since the item handler
     * could change in the meantime.
     *
     * @param chamberHandler what's currently in the pressure chamber
     * @return if this recipe is valid, a list of slots in the item handler where the ingredients can be found; otherwise, an empty list
     */
    Collection<Integer> findIngredients(@Nonnull IItemHandlerModifiable chamberHandler);

    /**
     * Get the input items for this recipe. This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    default List<Ingredient> getInputsForDisplay() {
        return Collections.emptyList();
    }

    /**
     * Get the output of this recipe, without crafting it.  This is intended for recipe display purposes by
     * JEI or any other recipe display mod, and also for testing insertability into the pressure chamber's output, so
     * the number of item stacks returned must at least be the same as the number of item stacks in the actual crafted output.
     */
    default NonNullList<ItemStack> getResultForDisplay() {
        return EMPTY_LIST;
    }

    /**
     * Check if the given item is a valid input item for this recipe.
     *
     * @param stack stack to check
     * @return true if this is a valid item, false otherwise
     */
    boolean isValidInputItem(ItemStack stack);

    /**
     * This method will be called when the recipe should output its items, and will only be called when
     * {@link #findIngredients(IItemHandlerModifiable)} returns a non-empty list of slot numbers, i.e. the necessary items
     * are definitely in the chamber.
     * The implementation is responsible for removing the items that have been used from the {@code chamberHandler}. The
     * implementation must also return the list of crafted items, for the Pressure Chamber to insert.
     *
     * @param chamberHandler items in the pressure chamber; should be modified to remove recipe input items.
     * @param ingredientSlots slots in the chamber handler where the ingredients can be found, as returned from {@link #findIngredients(IItemHandlerModifiable)}
     * @return the resulting items; these do not have to be copies - the Pressure Chamber itself will make sure they are copied
     */
    @Nonnull NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandlerModifiable chamberHandler, List<Integer> ingredientSlots);

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
