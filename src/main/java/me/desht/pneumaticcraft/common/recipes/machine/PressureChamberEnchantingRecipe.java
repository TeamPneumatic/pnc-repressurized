package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.AbstractRecipeSerializer;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PressureChamberEnchantingRecipe implements IPressureChamberRecipe {
    public static final ResourceLocation ID = RL("pressure_chamber_enchanting");

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
        if (recipeIngredients == null) return IPressureChamberRecipe.EMPTY_LIST;

        ItemStack enchantedTool = recipeIngredients[0];
        ItemStack enchantedBook = recipeIngredients[1];
        
        Map<Enchantment, Integer> bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        bookMap.forEach(enchantedTool::addEnchantment);
        
        enchantedBook.shrink(1);
        return NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.BOOK));
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
    public boolean isOutputItem(ItemStack stack) {
        return false;
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
