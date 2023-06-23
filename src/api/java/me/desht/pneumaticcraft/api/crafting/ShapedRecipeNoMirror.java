/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Just like a regular vanilla shaped recipe, but no horizontal mirroring.
 */
public class ShapedRecipeNoMirror extends ShapedRecipe {
    private final NonNullList<Ingredient> recipeItemsIn;

    public static final Serializer SERIALIZER = new Serializer();

    public ShapedRecipeNoMirror(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, CraftingBookCategory.MISC, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
        this.recipeItemsIn = recipeItemsIn;  // private in the superclass
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        for (int i = 0; i <= inv.getWidth() - this.getRecipeWidth(); ++i) {
            for (int j = 0; j <= inv.getHeight() - this.getRecipeHeight(); ++j) {
                if (this.checkMatch(inv, i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(CraftingContainer craftingInventory, int width, int height) {
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
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe r = super.fromJson(recipeId, json);
            return new ShapedRecipeNoMirror(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem(DummyRegistryAccess.INSTANCE));
        }

        @Nullable
        @Override
        public ShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapedRecipe r = super.fromNetwork(recipeId, buffer);
            return new ShapedRecipeNoMirror(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem(DummyRegistryAccess.INSTANCE));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
            super.toNetwork(buffer, recipe);
        }
    }
}
