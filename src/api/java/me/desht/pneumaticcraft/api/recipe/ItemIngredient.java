package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

/**
 * Represents an item-based ingredient for various PneumaticCraft machine recipes.
 * Can be a simple Itemstack, or a an Oredict key with associated quantity.
 */
public class ItemIngredient {
    private final ItemStack stack;
    private final String oredictKey;
    private final int amount;
    private String tooltipKey;

    public ItemIngredient(ItemStack stack) {
        this.stack = stack;
        this.oredictKey = null;
        this.amount = 0;
    }

    public ItemIngredient(Item item, int amount, int meta) {
        this(new ItemStack(item, amount, meta));
    }

    public ItemIngredient(String oredictKey, int amount) {
        Validate.isTrue(OreDictionary.doesOreNameExist(oredictKey), "invalid oredict key '" + oredictKey + "'");
        this.oredictKey = oredictKey;
        this.amount = amount;
        this.stack = ItemStack.EMPTY;
    }

    public ItemStack getSingleStack() {
        return oredictKey != null ?
                ItemHandlerHelper.copyStackWithSize(OreDictionary.getOres(oredictKey).get(0), amount) :
                stack;
    }

    public NonNullList<ItemStack> getStacks() {
        NonNullList<ItemStack> res = oredictKey != null ? OreDictionary.getOres(oredictKey) : NonNullList.from(ItemStack.EMPTY, stack);
        if (oredictKey != null) {
            res.forEach(stack -> stack.setCount(amount));
        }
        return res;
    }

    public int getItemAmount() {
        return oredictKey != null ? amount : stack.getCount();
    }

    public boolean isItemEqual(ItemStack stack) {
        if (oredictKey != null) {
            for (ItemStack s : OreDictionary.getOres(oredictKey)) {
                if (OreDictionary.itemMatches(s, stack, false))
                    return true;
            }
            return false;
        } else {
            return OreDictionary.itemMatches(this.stack, stack, false);
        }
    }

    public ItemIngredient setTooltip(String key) {
        this.tooltipKey = key;
        return this;
    }

    public String getTooltipKey() {
        return tooltipKey;
    }
}
