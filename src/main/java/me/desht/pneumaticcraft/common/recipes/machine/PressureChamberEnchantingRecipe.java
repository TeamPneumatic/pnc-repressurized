package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PressureChamberEnchantingRecipe implements IPressureChamberRecipe {
    public static final ResourceLocation ID = RL("pressure_chamber_enchanting");

    @Override
    public float getCraftingPressure() {
        return 2F;
    }

    @Override
    public Collection<Integer> findIngredients(IItemHandlerModifiable chamberHandler) {
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
            if (entry.getKey().canApply(enchantable)
                    && EnchantmentHelper.getEnchantmentLevel(entry.getKey(), enchantable) < entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(IItemHandlerModifiable chamberHandler, List<Integer> ingredientSlots) {
        ItemStack enchantedBook = chamberHandler.getStackInSlot(ingredientSlots.get(0));
        ItemStack enchantable = chamberHandler.getStackInSlot(ingredientSlots.get(1)).copy();

        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        bookMap.forEach((enchantment, level) -> {
            if (enchantment.canApply(enchantable)) enchantable.addEnchantment(enchantment, level);
        });

        chamberHandler.extractItem(ingredientSlots.get(0), 1, false);
        chamberHandler.extractItem(ingredientSlots.get(1), 1, false);
        return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK), enchantable);
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchBook.addEnchantment(Enchantments.FORTUNE, 1);

        return ImmutableList.of(Ingredient.fromItems(Items.DIAMOND_PICKAXE), Ingredient.fromStacks(enchBook));
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.addEnchantment(Enchantments.FORTUNE, 1);
        ItemStack book = new ItemStack(Items.BOOK);
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        return stack.getItem() == Items.ENCHANTED_BOOK || stack.isEnchantable();
    }

    @Override
    public String getTooltipKey(boolean input, int slot) {
        switch (slot) {
            case 0: return "gui.nei.tooltip.pressureEnchantItem";
            case 1: return "gui.nei.tooltip.pressureEnchantBook";
            case 2: return "gui.nei.tooltip.pressureEnchantItemOut";
            case 3: return "gui.nei.tooltip.pressureEnchantBookOut";
            default: return "";
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getRecipeType() {
        return ID;
    }

    public static class Serializer extends AbstractRecipeSerializer<PressureChamberEnchantingRecipe> {
        @Override
        public PressureChamberEnchantingRecipe read(ResourceLocation recipeId, JsonObject json) {
            return new PressureChamberEnchantingRecipe();
        }

        @Nullable
        @Override
        public PressureChamberEnchantingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            return new PressureChamberEnchantingRecipe();
        }

        @Override
        public void write(PacketBuffer buffer, PressureChamberEnchantingRecipe recipe) {
            super.write(buffer, recipe);
        }
    }
}
