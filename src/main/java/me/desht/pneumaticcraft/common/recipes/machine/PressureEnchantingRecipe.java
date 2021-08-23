package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

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
    public void write(PacketBuffer buffer) {
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESSURE_CHAMBER_ENCHANTING.get();
    }
}
