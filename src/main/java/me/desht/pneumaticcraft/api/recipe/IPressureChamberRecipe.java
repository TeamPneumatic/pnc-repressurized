package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.recipes.BasicPressureChamberRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IPressureChamberRecipe extends IModRecipe {
    NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();
    String NBT_TOOLTIP_KEY = "pnc:tooltip_key";

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
     * JEI or any other recipe display mod.  This is a list of lists so that alternates can be displayed where
     * multiple items match a tag, for example.
     */
    default List<List<ItemStack>> getInputsForDisplay() {
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
        if (!stack.hasTag()) stack.setTag(new CompoundNBT());
        stack.getTag().putString(NBT_TOOLTIP_KEY, key);
    }

    /**
     * Retrieve a translation key from the item for recipe display purposes.
     *
     * @param stack the stack to query
     * @return the translation key, or null if no key has been set
     */
    static String getTooltipKey(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_TOOLTIP_KEY, Constants.NBT.TAG_STRING)) return null;
        return stack.getTag().getString(NBT_TOOLTIP_KEY);
    }

    /**
     * Create a standard Pressure Chamber recipe.  Note that each input ingredient represents a
     * single item, but the same item can appear in the input list more than once.
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

    /**
     * Used for client-side sync'ing of recipes: do not call directly!
     * @param buf a packet buffer
     * @return a deserialised recipe
     */
    static IPressureChamberRecipe read(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        float pressure = buf.readFloat();
        int nInputs = buf.readVarInt();
        List<Ingredient> in = new ArrayList<>();
        for (int i = 0; i < nInputs; i++) {
            in.add(Ingredient.read(buf));
        }
        int nOutputs = buf.readVarInt();
        ItemStack[] out = new ItemStack[nOutputs];
        for (int i = 0; i < nOutputs; i++) {
            out[i] = buf.readItemStack();
        }
        return new BasicPressureChamberRecipe(id, in, pressure, out);
    }
}
