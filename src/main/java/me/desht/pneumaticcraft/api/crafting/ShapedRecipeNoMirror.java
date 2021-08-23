package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Just like a regular vanilla shaped recipe, but no horizontal mirroring.
 */
public class ShapedRecipeNoMirror extends ShapedRecipe {
    private final NonNullList<Ingredient> recipeItemsIn;

    public ShapedRecipeNoMirror(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
        this.recipeItemsIn = recipeItemsIn;  // private in the superclass
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        for (int i = 0; i <= inv.getWidth() - this.getRecipeWidth(); ++i) {
            for (int j = 0; j <= inv.getHeight() - this.getRecipeHeight(); ++j) {
                if (this.checkMatch(inv, i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(CraftingInventory craftingInventory, int width, int height) {
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                int k = i - width;
                int l = j - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.getRecipeWidth() && l < this.getRecipeHeight()) {
                    ingredient = this.recipeItemsIn.get(k + l * this.getRecipeWidth());
                }

                if (!ingredient.test(craftingInventory.getItem(i + j * craftingInventory.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_SHAPED_NO_MIRROR.get();
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe r = super.fromJson(recipeId, json);
            return new ShapedRecipeNoMirror(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem());
        }

        @Nullable
        @Override
        public ShapedRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            ShapedRecipe r = super.fromNetwork(recipeId, buffer);
            return new ShapedRecipeNoMirror(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem());
        }

        @Override
        public void toNetwork(PacketBuffer buffer, ShapedRecipe recipe) {
            super.toNetwork(buffer, recipe);
        }
    }
}
