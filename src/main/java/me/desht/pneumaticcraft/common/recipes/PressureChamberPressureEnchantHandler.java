package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PressureChamberPressureEnchantHandler implements IPressureChamberRecipe {
    @Override
    public float getCraftingPressure() {
        return 2F;
    }

    @Override
    public boolean isValidRecipe(ItemStackHandler chamberHandler) {
        return getRecipeIngredients(chamberHandler) != null;
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
                        // if the enchantment is applicable, AND the item doesn't have an existing enchantment of the
                        // same type which is equal or stronger to the book's enchantment level...
                        if (isApplicable(entry.getKey(), entry.getValue(), inputStack)) {
                            return new ItemStack[]{inputStack, enchantedBook};
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isApplicable(Enchantment ench, int level, ItemStack stack) {
        // if the enchantment is applicable, AND the item doesn't have an existing enchantment of the
        // same type which is equal or stronger to the book's enchantment level, AND the item doesn't have an
        // existing incompatible enchantment...
        return ench.canApply(stack)
                && EnchantmentHelper.getEnchantmentLevel(ench, stack) < level
                && EnchantmentHelper.getEnchantments(stack).entrySet().stream()
                .allMatch(entry -> ench == entry.getKey() || ench.isCompatibleWith(entry.getKey()));
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler chamberHandler) {
        ItemStack[] recipeIngredients = getRecipeIngredients(chamberHandler);
        if (recipeIngredients == null) return NonNullList.create(); // sanity check
        ItemStack enchantedTool = recipeIngredients[0];
        ItemStack enchantedBook = recipeIngredients[1];
        
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        Map<Enchantment, Integer> itemMap = EnchantmentHelper.getEnchantments(enchantedTool);

        List<Enchantment> toApply = new ArrayList<>();
        bookMap.forEach((ench, level) -> {
            if (isApplicable(ench, level, enchantedTool)) {
                itemMap.put(ench, bookMap.get(ench));
                toApply.add(ench);
            }
        });
        if (toApply.isEmpty()) return NonNullList.create();

        toApply.forEach(bookMap::remove);

        EnchantmentHelper.setEnchantments(itemMap, enchantedTool);

        enchantedBook.shrink(1);
        if (bookMap.isEmpty()) {
            return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK));
        } else {
            ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(bookMap, newBook);
            return NonNullList.from(ItemStack.EMPTY, newBook);
        }
    }

    @Override
    public List<ItemIngredient> getInput() {
        ItemIngredient pick = new ItemIngredient(Items.DIAMOND_PICKAXE, 1, 0).setTooltip("gui.nei.tooltip.pressureEnchantItem");

        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK, 1, 0);
        enchBook.addEnchantment(Enchantments.FORTUNE, 1);
        ItemIngredient book = new ItemIngredient(enchBook).setTooltip("gui.nei.tooltip.pressureEnchantBook");

        return ImmutableList.of(pick, book);
    }

    @Override
    public NonNullList<ItemStack> getResult() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE, 1, 0);
        pick.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.pressureEnchantItemOut");
        ItemStack book = new ItemStack(Items.BOOK);
        IPressureChamberRecipe.setTooltipKey(book, "gui.nei.tooltip.pressureEnchantBookOut");
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }
}
