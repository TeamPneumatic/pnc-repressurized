/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureDisenchantingRecipe extends PressureChamberRecipeImpl {
    public static final ResourceLocation ID = RL("pressure_chamber_disenchanting");

    public PressureDisenchantingRecipe(ResourceLocation id) {
        super(id, Collections.emptyList(), -0.75f);
    }

    @Override
    public Collection<Integer> findIngredients(@Nonnull IItemHandler chamberHandler) {
        int bookSlot = -1;
        int itemSlot = -1;

        // found slots will be { book, enchanted item } in that order

        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if (stack.getItem() == Items.BOOK) {
                bookSlot = i;
            } else {
                int minEnchantments = stack.getItem() == Items.ENCHANTED_BOOK ? 2 : 1;
                if (!blacklisted(stack) && EnchantmentHelper.getEnchantments(stack).size() >= minEnchantments) {
                    itemSlot = i;
                }
            }
            if (bookSlot >= 0 && itemSlot >= 0) return ImmutableList.of(bookSlot, itemSlot);
        }
        return Collections.emptyList();
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, List<Integer> ingredientSlots, boolean simulate) {
        ItemStack book = chamberHandler.extractItem(ingredientSlots.get(0), 1, simulate);
        ItemStack enchantedStack = chamberHandler.extractItem(ingredientSlots.get(1), 1, simulate);

        if (book.isEmpty() || enchantedStack.isEmpty()) return NonNullList.create();

        // take a random enchantment off the enchanted item...
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedStack);
        List<Enchantment> l = new ArrayList<>(enchantments.keySet());
        Enchantment strippedEnchantment = l.get(ThreadLocalRandom.current().nextInt(l.size()));
        int level = enchantments.get(strippedEnchantment);
        enchantments.remove(strippedEnchantment);
        // Workaround for setEnchantments on an Enchanted Book merging enchantments instead of setting them
        if (enchantedStack.getItem() == Items.ENCHANTED_BOOK) {
            enchantedStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        EnchantmentHelper.setEnchantments(enchantments, enchantedStack);

        // ...and create an enchanted book with it
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(strippedEnchantment, level), enchantedBook);

        return NonNullList.of(ItemStack.EMPTY, enchantedBook, enchantedStack);
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(Enchantments.BLOCK_FORTUNE, 1);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.BLOCK_FORTUNE, 1);
        enchantedBook.enchant(Enchantments.BLOCK_EFFICIENCY, 1);

        return ImmutableList.of(Ingredient.of(pick, enchantedBook), Ingredient.of(Items.BOOK));
    }

    @Override
    public List<List<ItemStack>> getResultsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
        ItemStack resultBook = new ItemStack(Items.ENCHANTED_BOOK);
        resultBook.enchant(Enchantments.BLOCK_FORTUNE, 1);
        return ImmutableList.of(ImmutableList.of(pick, enchantedBook), ImmutableList.of(resultBook));
    }

    @Override
    public List<Set<RecipeSlot>> getSyncGroupsForDisplay() {
        return ImmutableList.of(ImmutableSet.of(
                new RecipeSlot(true, 0),
                new RecipeSlot(false, 0)
        ));
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        return stack.getItem() == Items.BOOK
                || stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0;
    }

    @Override
    public String getTooltipKey(boolean input, int slot) {
        switch (slot) {
            case 0: return "pneumaticcraft.gui.nei.tooltip.vacuumEnchantItem";
            case 2: return "pneumaticcraft.gui.nei.tooltip.vacuumEnchantItemOut";
            case 3: return "pneumaticcraft.gui.nei.tooltip.vacuumEnchantBookOut";
            default: return "";
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESSURE_CHAMBER_DISENCHANTING.get();
    }

    private boolean blacklisted(ItemStack stack) {
        if (stack.getItem().getRegistryName() != null) {
            String name = stack.getItem().getRegistryName().toString();
            return PNCConfig.Common.Machines.disenchantingBlacklist.stream().anyMatch(name::startsWith);
        }
        return false;
    }
}
