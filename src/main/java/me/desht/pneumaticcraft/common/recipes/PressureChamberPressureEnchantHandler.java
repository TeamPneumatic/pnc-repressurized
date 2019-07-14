package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

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
            if ((inputStack.isEnchantable() || inputStack.isEnchanted()) && inputStack.getItem() != Items.ENCHANTED_BOOK) {
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
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler chamberHandler) {
        ItemStack[] recipeIngredients = getRecipeIngredients(chamberHandler);
        ItemStack enchantedTool = recipeIngredients[0];
        ItemStack enchantedBook = recipeIngredients[1];
        
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        bookMap.forEach(enchantedTool::addEnchantment);
        
        enchantedBook.shrink(1);
        return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK));
    }

    @Override
    public List<ItemIngredient> getInput() {
        ItemIngredient pick = new ItemIngredient(Items.DIAMOND_PICKAXE, 1, 0).setTooltip("gui.nei.tooltip.pressureEnchantItem");

        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchBook.addEnchantment(Enchantments.FORTUNE, 1);
        ItemIngredient book = new ItemIngredient(enchBook).setTooltip("gui.nei.tooltip.pressureEnchantBook");

        return ImmutableList.of(pick, book);
    }

    @Override
    public NonNullList<ItemStack> getResult() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.pressureEnchantItemOut");
        ItemStack book = new ItemStack(Items.BOOK);
        IPressureChamberRecipe.setTooltipKey(book, "gui.nei.tooltip.pressureEnchantBookOut");
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }
}
