package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

public class PressureChamberDisenchantingRecipe implements IPressureChamberRecipe {
    public static final ResourceLocation ID = RL("pressure_chamber_disenchanting");

    @Override
    public float getCraftingPressure() {
        return -0.75F;
    }

    @Override
    public Collection<Integer> findIngredients(IItemHandlerModifiable chamberHandler) {
        int bookSlot = -1;
        int itemSlot = -1;

        // found slots will be { book, enchanted item } in that order

        for (int i = 0; i < chamberHandler.getSlots(); i++) {
            ItemStack stack = chamberHandler.getStackInSlot(i);
            if (stack.getItem() == Items.BOOK) {
                bookSlot = i;
            } else if (stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0) {
                itemSlot = i;
            }
            if (bookSlot >= 0 && itemSlot >= 0) return ImmutableList.of(bookSlot, itemSlot);
        }
        return Collections.emptyList();
    }

    @Override
    public NonNullList<ItemStack> craftRecipe(IItemHandlerModifiable chamberHandler, List<Integer> ingredientSlots) {
        ItemStack book = chamberHandler.extractItem(ingredientSlots.get(0), 1, false);
        ItemStack enchantedStack = chamberHandler.extractItem(ingredientSlots.get(1), 1, false);

        if (book.isEmpty() || enchantedStack.isEmpty()) return NonNullList.create();

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

        return NonNullList.from(ItemStack.EMPTY, enchantedBook, enchantedStack);
    }

    @Override
    public List<Ingredient> getInputsForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.addEnchantment(Enchantments.FORTUNE, 1);

        return ImmutableList.of(Ingredient.fromStacks(pick), Ingredient.fromItems(Items.BOOK));
    }

    @Override
    public NonNullList<ItemStack> getResultForDisplay() {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(Enchantments.FORTUNE, 1);
        return NonNullList.from(ItemStack.EMPTY, pick, book);
    }

    @Override
    public boolean isValidInputItem(ItemStack stack) {
        return stack.getItem() == Items.BOOK
                || stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0;
    }

    @Override
    public String getTooltipKey(boolean input, int slot) {
        switch (slot) {
            case 0: return "gui.nei.tooltip.vacuumEnchantItem";
            case 2: return "gui.nei.tooltip.vacuumEnchantItemOut";
            case 3: return "gui.nei.tooltip.vacuumEnchantBookOut";
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

    public static class Serializer extends AbstractRecipeSerializer<PressureChamberDisenchantingRecipe> {
        @Override
        public PressureChamberDisenchantingRecipe read(ResourceLocation recipeId, JsonObject json) {
            return new PressureChamberDisenchantingRecipe();
        }

        @Nullable
        @Override
        public PressureChamberDisenchantingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            return new PressureChamberDisenchantingRecipe();
        }

        @Override
        public void write(PacketBuffer buffer, PressureChamberDisenchantingRecipe recipe) {
            super.write(buffer, recipe);
        }
    }
}
