package me.desht.pneumaticcraft.common.recipes.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PressureChamberDisenchantingRecipe implements IPressureChamberRecipe {
    public static final ResourceLocation ID = RL("pressure_chamber_disenchanting");

    @Override
    public float getCraftingPressure() {
        return -0.75F;
    }

    @Override
    public boolean isValidRecipe(ItemStackHandler chamberHandler) {
        return !getDisenchantableItem(chamberHandler).isEmpty() && !getBook(chamberHandler).isEmpty();
    }
    
    private ItemStack getDisenchantableItem(ItemStackHandler inputStacks){
        return new ItemStackHandlerIterable(inputStacks)
                        .stream()
                        .filter(stack -> stack.getItem() != Items.ENCHANTED_BOOK && EnchantmentHelper.getEnchantments(stack).size() > 0)
                        .findFirst()
                        .orElse(ItemStack.EMPTY);
    }
    
    private ItemStack getBook(ItemStackHandler inputStacks){
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
    public String getTooltipKey(boolean input, int slot) {
        switch (slot) {
            case 0: return "gui.nei.tooltip.vacuumEnchantItem";
            case 2: return "gui.nei.tooltip.vacuumEnchantItemOut";
            case 3: return "gui.nei.tooltip.vacuumEnchantBookOut";
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
