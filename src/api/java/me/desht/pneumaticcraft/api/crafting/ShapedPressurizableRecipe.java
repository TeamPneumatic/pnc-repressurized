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
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Just like a regular shaped recipe, but any air in any input ingredients is added up and put into the output item.
 */
public class ShapedPressurizableRecipe extends ShapedRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    private ShapedPressurizableRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack newOutput = this.getResultItem().copy();

        newOutput.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(outputHandler -> {
            int totalAir = 0;
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = inv.getItem(i);
                totalAir += stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(IAirHandler::getAir).orElse(0);
            }
            outputHandler.addAir(totalAir);
        });

        return newOutput;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe r = super.fromJson(recipeId, json);
            return new ShapedPressurizableRecipe(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem());
        }

        @Nullable
        @Override
        public ShapedRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            ShapedRecipe r = super.fromNetwork(recipeId, buffer);
            return new ShapedPressurizableRecipe(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem());
        }

        @Override
        public void toNetwork(PacketBuffer buffer, ShapedRecipe recipe) {
            super.toNetwork(buffer, recipe);
        }
    }
}
