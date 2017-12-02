package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PressureChamberPressureEnchantHandler implements IPressureChamberRecipe {

    @Override
    public float getCraftingPressure() {
        return 2F;
    }

    @Override
    public boolean isValidRecipe(ItemStackHandler inputStacks) {
        return getRecipeIngredients(inputStacks) != null;
    }
    
    private ItemStack[] getRecipeIngredients(ItemStackHandler inputStacks) {
        List<ItemStack> enchantedBooks = new ItemStackHandlerIterable(inputStacks)
                                                    .stream()
                                                    .filter(book -> book.getItem() == Items.ENCHANTED_BOOK)
                                                    .collect(Collectors.toList());

        if (enchantedBooks.isEmpty()) return null;

        for (ItemStack inputStack : new ItemStackHandlerIterable(inputStacks)) {
            if ((inputStack.isItemEnchantable() || inputStack.isItemEnchanted()) && inputStack.getItem() != Items.ENCHANTED_BOOK) {
                for (ItemStack enchantedBook : enchantedBooks) {
                    Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
                    for (Map.Entry<Enchantment, Integer> entry : bookMap.entrySet()) {
                        if (entry.getKey().canApply(inputStack)) {
                            return new ItemStack[]{ inputStack, enchantedBook};
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler inputStacks) {
        ItemStack[] recipeIngredients = getRecipeIngredients(inputStacks);
        ItemStack enchantedTool = recipeIngredients[0];
        ItemStack enchantedBook = recipeIngredients[1];
        
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        bookMap.forEach((enchant, lvl) -> enchantedTool.addEnchantment(enchant, lvl));
        
        enchantedBook.shrink(1);
        return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK));
    }
}
