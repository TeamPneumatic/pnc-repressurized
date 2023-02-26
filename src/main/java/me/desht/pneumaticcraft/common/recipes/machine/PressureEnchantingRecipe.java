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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
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
    public IntCollection findIngredients(@Nonnull IItemHandler chamberHandler) {
        // found slots will be { enchanted book, enchantable item } in that order

        IntList bookSlots = new IntArrayList();
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
                        return IntList.of(bookSlot, i);
                    }
                }
            }
        }
        return IntList.of();
    }

    private boolean isApplicable(ItemStack enchantedBook, ItemStack enchantable) {
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        // if the enchantment is applicable, AND the item doesn't have an existing enchantment of the
        // same type which is equal to or stronger than the book's enchantment level...
        return bookMap.entrySet().stream()
                .anyMatch(entry -> enchantable.canApplyAtEnchantingTable(entry.getKey())
                        && enchantable.getEnchantmentLevel(entry.getKey()) < entry.getValue());
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        ItemStack enchantedBook = chamberHandler.getStackInSlot(ingredientSlots.getInt(0));
        ItemStack enchantable = ItemHandlerHelper.copyStackWithSize(chamberHandler.getStackInSlot(ingredientSlots.getInt(1)), 1);

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
            // all the enchantments could transfer
            newBook = new ItemStack(Items.BOOK);
        } else {
            // some enchantments could transfer
            newBook = new ItemStack(Items.ENCHANTED_BOOK);
            bookEnchantments.forEach(newBook::enchant);
        }

        chamberHandler.extractItem(ingredientSlots.getInt(0), 1, simulate);
        chamberHandler.extractItem(ingredientSlots.getInt(1), 1, simulate);
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
    public String getTooltipKey(boolean input, String slotName) {
        return switch (slotName) {
            case "in0" -> "pneumaticcraft.gui.nei.tooltip.pressureEnchantItem";
            case "in1" -> "pneumaticcraft.gui.nei.tooltip.pressureEnchantBook";
            case "out0" -> "pneumaticcraft.gui.nei.tooltip.pressureEnchantItemOut";
            case "out1" -> "pneumaticcraft.gui.nei.tooltip.pressureEnchantBookOut";
            default -> "";
        };
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PRESSURE_CHAMBER_ENCHANTING.get();
    }
}
