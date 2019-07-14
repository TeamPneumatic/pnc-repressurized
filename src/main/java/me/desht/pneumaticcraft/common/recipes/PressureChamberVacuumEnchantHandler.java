package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
    public boolean isValidRecipe(ItemStackHandler chamberHandler) {
        return !getDisenchantableItem(chamberHandler).isEmpty() && !getBook(chamberHandler).isEmpty();

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
    public NonNullList<ItemStack> craftRecipe(ItemStackHandler chamberHandler) {
        ItemStack enchantedStack = getDisenchantableItem(chamberHandler);
        getBook(chamberHandler).shrink(1);
        
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

    @Override
    public List<ItemIngredient> getInput() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.addEnchantment(Enchantments.FORTUNE, 1);
        ItemIngredient pick = new ItemIngredient(stack).setTooltip("gui.nei.tooltip.vacuumEnchantItem");
        ItemIngredient book = new ItemIngredient(Items.BOOK, 1, 0);
        return ImmutableList.of(pick, book);
    }

    @Override
    public NonNullList<ItemStack> getResult() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        IPressureChamberRecipe.setTooltipKey(pick, "gui.nei.tooltip.vacuumEnchantItemOut");
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(Enchantments.FORTUNE, 1);
        IPressureChamberRecipe.setTooltipKey(book, "gui.nei.tooltip.vacuumEnchantBookOut");
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }
}
