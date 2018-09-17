package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.OreDictionaryHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExplosionCraftingRecipe {
    public static List<ExplosionCraftingRecipe> recipes = new ArrayList<>();

    private final ItemStack input;
    private final String oreDictKey;
    private final ItemStack output;
    private final int lossRate;

    public static void addExplosionRecipe(ItemStack input, ItemStack output, int lossRate) {
        PneumaticRegistry.getInstance().getRecipeRegistry().registerExplosionCraftingRecipe(input, output, lossRate);
    }

    public static void addExplosionRecipe(String oreDictKey, ItemStack output, int lossRate) {
        PneumaticRegistry.getInstance().getRecipeRegistry().registerExplosionCraftingRecipe(oreDictKey, output, lossRate);
    }

    public ExplosionCraftingRecipe(ItemStack input, ItemStack output, int lossRate) {
        this.input = input;
        this.output = output;
        this.lossRate = lossRate;
        this.oreDictKey = null;
    }

    public ExplosionCraftingRecipe(String oreDictKey, ItemStack output, int lossRate) {
        this.input = ItemStack.EMPTY;
        this.output = output;
        this.lossRate = lossRate;
        this.oreDictKey = oreDictKey;
    }

    public ItemStack getInput() {
        return input;
    }

    public String getOreDictKey() {
        return oreDictKey;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getLossRate() {
        return lossRate;
    }

    public static ItemStack tryToCraft(ItemStack stack) {
        for (ExplosionCraftingRecipe recipe : recipes) {
            if (recipe.match(stack)) {
                return recipe.createOutput(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack createOutput(ItemStack stack) {
        Random rand = new Random();
        if (stack.getCount() >= 3 || rand.nextDouble() >= lossRate / 100D) {
            ItemStack newStack = new ItemStack(output.getItem(), stack.getCount(), output.getItemDamage());
            if (stack.getCount() >= 3) {
                newStack.setCount((int) (stack.getCount() * (rand.nextDouble() * Math.min(lossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - lossRate * 0.01D) - lossRate * 0.01D))));
            }
            return newStack;
        }
        return ItemStack.EMPTY;
    }

    private boolean match(ItemStack ingredient) {
        return !input.isEmpty() && ItemStack.areItemsEqual(input, ingredient)
                || oreDictKey != null && OreDictionaryHelper.isItemEqual(oreDictKey, ingredient);
    }
}
