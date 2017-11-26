package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PressureChamberVacuumEnchantHandler implements IPressureChamberRecipe {
    @Override
    public float getCraftingPressure() {
        return -0.75F;
    }

    @Override
    public ItemStack[] isValidRecipe(ItemStack[] inputStacks) {
        int enchantmentCount = 0;
        for (ItemStack stack : inputStacks) {
            if (stack.getItem() == Items.ENCHANTED_BOOK) continue;
            enchantmentCount = EnchantmentHelper.getEnchantments(stack).size();
            if (enchantmentCount > 0) break;
        }
        if (enchantmentCount == 0) return null;
        for (ItemStack stack : inputStacks) {
            if (stack.getItem() == Items.BOOK) {
                return new ItemStack[]{new ItemStack(Items.BOOK)};
            }
        }
        return null;

    }

    @Override
    public ItemStack[] craftRecipe(ItemStack[] inputStacks, ItemStack[] removedStacks) {
        ItemStack enchantedStack = ItemStack.EMPTY;
        int enchantmentCount;

        // find an enchanted item (other than an enchanted book)
        for (ItemStack stack : inputStacks) {
            if (stack.getItem() == Items.ENCHANTED_BOOK) continue;
            enchantmentCount = EnchantmentHelper.getEnchantments(stack).size();
            if (enchantmentCount > 0) {
                enchantedStack = stack;
                break;
            }
        }
        if (enchantedStack.isEmpty()) {
            Log.error("[Pressure Chamber Vacuum Enchantment Handler] No enchanted stack found!");
            return null;
        }

        // take a random enchantment off the enchanted item...
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedStack);
        List<Enchantment> l = new ArrayList<>(enchantments.keySet());
        Enchantment strippedEnchantment = l.get(new Random().nextInt(l.size()));
        int level = enchantments.get(strippedEnchantment);
        enchantments.remove(strippedEnchantment);
        EnchantmentHelper.setEnchantments(enchantments, enchantedStack);

        // ...and create an enchanted book with it
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(strippedEnchantment, level), enchantedBook);

        return new ItemStack[]{enchantedBook};
    }

    @Override
    public boolean shouldRemoveExactStacks() {
        return false;
    }

}
