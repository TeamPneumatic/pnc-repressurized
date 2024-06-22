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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PressureEnchantingRecipe extends PressureChamberRecipeImpl {
    public static final ResourceLocation ID = RL("pressure_chamber_enchanting");

    public PressureEnchantingRecipe(CraftingBookCategory category) {
        super(Collections.emptyList(), 2F, List.of());
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
        ItemEnchantments bookMap = EnchantmentHelper.getEnchantmentsForCrafting(enchantedBook);
        // if the enchantment is applicable, AND the item doesn't have an existing enchantment of the
        // same type which is equal to or stronger than the book's enchantment level...
        return bookMap.entrySet().stream()
                .anyMatch(entry -> enchantable.isPrimaryItemFor(entry.getKey())
                        && enchantable.getEnchantmentLevel(entry.getKey()) < entry.getIntValue());
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(@Nonnull IItemHandler chamberHandler, IntList ingredientSlots, boolean simulate) {
        ItemStack enchantedBook = chamberHandler.getStackInSlot(ingredientSlots.getInt(0));
        ItemStack enchantable = chamberHandler.getStackInSlot(ingredientSlots.getInt(1)).copyWithCount(1);

        ItemEnchantments.Mutable bookEnchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(enchantedBook));
        Set<Holder<Enchantment>> itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(enchantable).keySet();

        Set<Holder<Enchantment>> toTransfer = new HashSet<>();

        bookEnchantments.keySet().forEach(entry -> {
            Enchantment enchantment = entry.value();
            if (enchantment.canEnchant(enchantable) && itemEnchantments.stream().allMatch(e -> Enchantment.areCompatible(e, entry))) {
                enchantable.enchant(entry, bookEnchantments.getLevel(entry));
                toTransfer.add(entry);
            }
        });

        if (toTransfer.isEmpty()) {
            // no enchantments could be transferred
            return NonNullList.create();
        }

        bookEnchantments.keySet().removeAll(toTransfer);

        ItemStack newBook;
        if (bookEnchantments.keySet().isEmpty()) {
            // all the enchantments could transfer
            newBook = new ItemStack(Items.BOOK);
        } else {
            // some enchantments could transfer
            newBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(newBook, bookEnchantments.toImmutable());
        }

        chamberHandler.extractItem(ingredientSlots.getInt(0), 1, simulate);
        chamberHandler.extractItem(ingredientSlots.getInt(1), 1, simulate);
        return NonNullList.of(ItemStack.EMPTY, newBook, enchantable);
    }

    @Override
    public List<List<ItemStack>> getInputsForDisplay(HolderLookup.Provider provider) {
        Holder<Enchantment> fortune = EnchantmentUtils.getEnchantment(provider, Enchantments.FORTUNE);
        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchBook.enchant(fortune, 1);

        return List.of(List.of(new ItemStack(Items.DIAMOND_PICKAXE)), List.of(enchBook));
    }

    @Override
    public List<ItemStack> getSingleResultsForDisplay(HolderLookup.Provider provider) {
        Holder<Enchantment> fortune = EnchantmentUtils.getEnchantment(provider, Enchantments.FORTUNE);
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(fortune, 1);

        return List.of(pick, new ItemStack(Items.BOOK));
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PRESSURE_CHAMBER_ENCHANTING.get();
    }

}
