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
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureEnchantingRecipe extends PressureChamberRecipeImpl {
    public static final ResourceLocation ID = RL("pressure_chamber_enchanting");

    public PressureEnchantingRecipe(ResourceLocation id) {
        super(id, Collections.emptyList(), 2F);
    }

    @Override
    public Collection<Integer> findIngredients(@Nonnull IItemHandler chamberHandler) {
        // found slots will be { enchanted book, enchantable item } in that order

        List<Integer> bookSlots = new ArrayList<>();
        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                bookSlots.add(i);
            }
        }

        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if (stack.isEnchantable() || stack.isEnchanted()) {
                for (int bookSlot : bookSlots) {
                    ItemStack enchantedBook = chamberHandler.getStackInSlot(bookSlot);
                    if (isApplicable(enchantedBook, stack)) {
                        return ImmutableList.of(bookSlot, i);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean isApplicable(ItemStack enchantedBook, ItemStack enchantable) {
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        for (Map.Entry<Enchantment, Integer> entry : bookMap.entrySet()) {
            // if the enchantment is applicable, AND the item doesn't have an existing enchantment of the
            // same type which is equal to or stronger than the book's enchantment level...

            if (enchantable.canApplyAtEnchantingTable(entry.getKey())
                    && EnchantmentHelper.getItemEnchantmentLevel(entry.getKey(), enchantable) < entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, List<Integer> ingredientSlots, boolean simulate) {
        ItemStack enchantedBook = chamberHandler.getStackInSlot(ingredientSlots.get(0));
        ItemStack enchantable = ItemHandlerHelper.copyStackWithSize(chamberHandler.getStackInSlot(ingredientSlots.get(1)), 1);

        Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(enchantedBook);
        Set<Enchantment> itemEnchantments = EnchantmentHelper.getEnchantments(enchantable).keySet();
        List<Enchantment> toTransfer = new ArrayList<>();
        bookEnchantments.forEach((enchantment, level) -> {
            if (enchantment.canEnchant(enchantable) && itemEnchantments.stream().allMatch(e -> e.isCompatibleWith(enchantment))) {
                enchantable.enchant(enchantment, level);
                toTransfer.add(enchantment);
            }
        });
        if (toTransfer.isEmpty()) return NonNullList.create(); // no enchantments could be transferred
        toTransfer.forEach(bookEnchantments::remove);
        ItemStack newBook;
        if (bookEnchantments.isEmpty()) {
            // all of the enchantments could transfer
            newBook = new ItemStack(Items.BOOK);
        } else {
            // some of the enchantments could transfer
            newBook = new ItemStack(Items.ENCHANTED_BOOK);
            bookEnchantments.forEach(newBook::enchant);
        }

        chamberHandler.extractItem(ingredientSlots.get(0), 1, simulate);
        chamberHandler.extractItem(ingredientSlots.get(1), 1, simulate);
        return NonNullList.of(ItemStack.EMPTY, newBook, enchantable);
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchBook.enchant(Enchantments.BLOCK_FORTUNE, 1);

        return ImmutableList.of(Ingredient.of(Items.DIAMOND_PICKAXE), Ingredient.of(enchBook));
    }

    @Override
    public List<ItemStack> getSingleResultsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(Enchantments.BLOCK_FORTUNE, 1);
        ItemStack book = new ItemStack(Items.BOOK);
        return NonNullList.of(ItemStack.EMPTY, pick, book);
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        return stack.getItem() == Items.ENCHANTED_BOOK || stack.isEnchantable();
    }

    @Override
    public String getTooltipKey(boolean input, int slot) {
        switch (slot) {
            case 0: return "pneumaticcraft.gui.nei.tooltip.pressureEnchantItem";
            case 1: return "pneumaticcraft.gui.nei.tooltip.pressureEnchantBook";
            case 2: return "pneumaticcraft.gui.nei.tooltip.pressureEnchantItemOut";
            case 3: return "pneumaticcraft.gui.nei.tooltip.pressureEnchantBookOut";
            default: return "";
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESSURE_CHAMBER_ENCHANTING.get();
    }
}
