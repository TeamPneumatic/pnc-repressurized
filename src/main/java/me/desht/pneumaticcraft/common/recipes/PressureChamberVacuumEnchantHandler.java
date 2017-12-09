package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

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
    public boolean isValidRecipe(ItemStackHandler inputStacks) {
        return !getDisenchantableItem(inputStacks).isEmpty() && !getBook(inputStacks).isEmpty();

    }
    
    public ItemStack getDisenchantableItem(ItemStackHandler inputStacks){
        return new ItemStackHandlerIterable(inputStacks)
                        .stream()
                        .filter(stack -> stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
    }
    
    public ItemStack getBook(ItemStackHandler inputStacks){
        return new ItemStackHandlerIterable(inputStacks)
                        .stream()
                        .filter(stack -> stack.getItem() == Items.BOOK)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler inputStacks) {
        ItemStack enchantedStack = getDisenchantableItem(inputStacks);
        getBook(inputStacks).shrink(1);
        
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

        return NonNullList.from(ItemStack.EMPTY, enchantedBook);
    }
}
