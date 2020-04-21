package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public abstract class PressureChamberRecipe extends PneumaticCraftRecipe {
    protected PressureChamberRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Returns the minimum pressure required to craft the recipe. Negative pressures are also acceptable; in this
     * case the pressure chamber's pressure must be <strong>lower</strong> than the required pressure.
     *
     * @return threshold pressure
     */
    public abstract float getCraftingPressure();

    /**
     * Try to find the ingredients for this recipe in the given item handler, which is all of the items currently in the
     * pressure chamber. Returns a collection of slot indices into the item handler, which should be passed promptly to
     * {@link #craftRecipe(IItemHandler, List)} and <strong>not cached across ticks</strong>, since the item handler
     * could change in the meantime.
     *
     * @param chamberHandler what's currently in the pressure chamber
     * @return if this recipe is valid, a list of slots in the item handler where the ingredients can be found; otherwise, an empty list
     */
    public abstract Collection<Integer> findIngredients(@Nonnull IItemHandler chamberHandler);

    /**
     * Get the input items for this recipe. This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    public abstract List<Ingredient> getInputsForDisplay();

    /**
     * Get the output of this recipe, without crafting it.  This is intended for recipe display purposes by
     * JEI, Patchouli, or any other recipe display mod.
     * <p>
     * This is also used for testing insertability into the Pressure Chamber's output, so the number of item stacks
     * returned must at least be the same as the number of item stacks in the actual crafted output, even if the results
     * aren't exactly the same as an actual craft of the recipe.
     */
    public abstract NonNullList<ItemStack> getResultsForDisplay();

    /**
     * Check if the given item is a valid input item for this recipe.  This should also true even if the number of items
     * in the passed item stack is smaller than the number required by the recipe; this is testing for item type, not
     * item count.
     *
     * @param stack item stack to check
     * @return true if this is a valid item, false otherwise
     */
    public abstract boolean isValidInputItem(ItemStack stack);

    /**
     * This method is called when the Pressure Chamber is ready to craft with this recipe, and will only be called when
     * {@link #findIngredients(IItemHandler)} returns a non-empty list of slot numbers, i.e. the necessary
     * items are definitely in the chamber.
     * The implementation is responsible for removing the items that have been used from the {@code chamberHandler}.
     * The implementation must also return the list of crafted items, for the Pressure Chamber to insert into its
     * output item handler.
     *
     * @param chamberHandler items in the pressure chamber; should be modified to remove recipe input items.
     * @param ingredientSlots slots in the chamber handler where the ingredients can be found, as returned from {@link #findIngredients(IItemHandler)}
     * @return the resulting items; these do not have to be copies, since the Pressure Chamber itself will insert copies of these items
     */
    @Nonnull
    public abstract NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, List<Integer> ingredientSlots);

    /**
     * Return a translation key for a supplementary tooltip to be displayed on the ingredient or resulting item.  For
     * use in recipe display systems such as JEI.
     *
     * @param input true if this is an input item, false if an output item
     * @param slot the slot number
     * @return a tooltip translation key, or "" for no tooltip
     */
    public String getTooltipKey(boolean input, int slot) {
        return "";
    }
}
