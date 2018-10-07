package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IPressureChamberRecipe {
    List<ItemIngredient> EMPTY_INPUT = Collections.emptyList();
    NonNullList<ItemStack> EMPTY_RESULT = NonNullList.create();
    String NBT_TOOLTIP_KEY = "pnc:tooltip_key";

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
     * @param chamberHandler What's currently in the pressure chamber
     * @return true if this recipe is valid for what's in the chamber (note: pressure checks are not handled here)
     */
    boolean isValidRecipe(@Nonnull ItemStackHandler chamberHandler);

    /**
     * Get the input items for this recipe. This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    default List<ItemIngredient> getInput() {
        return EMPTY_INPUT;
    }
    /**
     * Get the output of this recipe, without crafting it.  This is primarily intended for recipe display purposes by
     * JEI or any other recipe display mod.
     */
    default NonNullList<ItemStack> getResult() {
        return EMPTY_RESULT;
    }

    /**
     * This method will be called when the recipe should output its items (after isValidRecipe returns true).  The
     * implementation is responsible for removing the items that have been used from the {@code chamberHandler}. Output
     * items will be automatically placed into the {@code chamberHandler} but this implementation should also return
     * the list of crafted items, in case they do not all fit in the pressure chamber.
     *
     * @param chamberHandler  items in the pressure chamber; should be modified to remove recipe input items.
     * @return the resulting items; these do not have to be copies - the Pressure Chamber itself will make sure they are copied
     */
    @Nonnull NonNullList<ItemStack> craftRecipe(@Nonnull ItemStackHandler chamberHandler);

    /**
     * Store a translation key to be displayed as an item tooltip by JEI (or in theory any other recipe display system)
     *
     * @param stack the stack to modify
     * @param key the translation key
     */
    static void setTooltipKey(ItemStack stack, String key) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(NBT_TOOLTIP_KEY, key);
    }

    /**
     * Retrieve a translation key from the item for recipe display purposes.
     *
     * @param stack the stack to query
     * @return the translation key, or null if no key has been set
     */
    static String getTooltipKey(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(NBT_TOOLTIP_KEY, Constants.NBT.TAG_STRING)) return null;
        return stack.getTagCompound().getString(NBT_TOOLTIP_KEY);
    }

}
