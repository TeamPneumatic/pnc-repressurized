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
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureDisenchantingRecipe extends PressureChamberRecipeImpl {
    public static final ResourceLocation ID = RL("pressure_chamber_disenchanting");

    public PressureDisenchantingRecipe(CraftingBookCategory category) {
        super(Collections.emptyList(), -0.75f, List.of());
    }

    @Override
    public IntCollection findIngredients(@Nonnull IItemHandler chamberHandler) {
        int bookSlot = -1;
        int itemSlot = -1;

        // found slots will be { book, enchanted item } in that order

        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if (stack.getItem() == Items.BOOK) {
                bookSlot = i;
            } else {
                int minEnchantments = stack.getItem() == Items.ENCHANTED_BOOK ? 2 : 1;
                if (!blacklisted(stack) && EnchantmentHelper.getEnchantmentsForCrafting(stack).size() >= minEnchantments) {
                    itemSlot = i;
                }
            }
            if (bookSlot >= 0 && itemSlot >= 0) return IntList.of(bookSlot, itemSlot);
        }
        return IntList.of();
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        ItemStack book = chamberHandler.extractItem(ingredientSlots.getInt(0), 1, simulate);
        ItemStack enchantedStack = chamberHandler.extractItem(ingredientSlots.getInt(1), 1, simulate);

        if (book.isEmpty() || enchantedStack.isEmpty()) return NonNullList.create();

        // take a random enchantment off the enchanted item...
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(enchantedStack));
        List<Holder<Enchantment>> l = new ArrayList<>(enchantments.keySet());
        Holder<Enchantment> strippedEnchantment = l.get(ThreadLocalRandom.current().nextInt(l.size()));
        int level = enchantments.getLevel(strippedEnchantment.value());
        enchantments.removeIf(e -> e.value() == strippedEnchantment.value());
        // Workaround for setEnchantments on an Enchanted Book merging enchantments instead of setting them
        if (enchantedStack.getItem() == Items.ENCHANTED_BOOK) {
            enchantedStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        EnchantmentHelper.setEnchantments(enchantedStack, enchantments.toImmutable());

        // ...and create an enchanted book with it
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable newEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        newEnchantments.set(strippedEnchantment.value(), level);
        EnchantmentHelper.setEnchantments(enchantedBook, newEnchantments.toImmutable());

        return NonNullList.of(ItemStack.EMPTY, enchantedBook, enchantedStack);
    }

    @Override
    public List<List<ItemStack>> getInputsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(Enchantments.FORTUNE, 1);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.FORTUNE, 1);
        enchantedBook.enchant(Enchantments.EFFICIENCY, 1);

        return List.of(List.of(pick), List.of(new ItemStack(Items.BOOK)));
    }

    @Override
    public List<List<ItemStack>> getResultsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(Enchantments.EFFICIENCY, 1);
        ItemStack resultBook = new ItemStack(Items.ENCHANTED_BOOK);
        resultBook.enchant(Enchantments.FORTUNE, 1);

        return List.of(List.of(pick, enchantedBook), List.of(resultBook));
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
                || stack.getItem() != Items.ENCHANTED_BOOK && !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
    }

    @Override
    public String getTooltipKey(boolean input, String slotName) {
        return switch (slotName) {
            case "in0" -> "pneumaticcraft.gui.nei.tooltip.vacuumEnchantItem";
            case "out0" -> "pneumaticcraft.gui.nei.tooltip.vacuumEnchantItemOut";
            case "out1" -> "pneumaticcraft.gui.nei.tooltip.vacuumEnchantBookOut";
            default -> "";
        };
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PRESSURE_CHAMBER_DISENCHANTING.get();
    }

    private boolean blacklisted(ItemStack stack) {
        List<String> blackList = ConfigHelper.common().machines.disenchantingBlacklist.get();
        return PneumaticCraftUtils.getRegistryName(stack.getItem())
                .map(name -> blackList.stream().anyMatch(element -> element.startsWith(name.toString())))
                .orElse(false);
    }

}
