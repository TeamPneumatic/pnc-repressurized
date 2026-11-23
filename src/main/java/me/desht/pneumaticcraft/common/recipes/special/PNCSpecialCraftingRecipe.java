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

package me.desht.pneumaticcraft.common.recipes.special;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.function.Function;

/**
 * Base for PneumaticCraft's special (code-driven) shapeless crafting recipes. These only have a crafting book
 * category in their JSON, like the old {@code SimpleCraftingRecipeSerializer} recipes, and provide a shapeless
 * display so recipe viewers can still show them.
 */
public abstract class PNCSpecialCraftingRecipe extends CustomRecipe {
    private final CraftingBookCategory category;

    protected PNCSpecialCraftingRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                displayIngredients(), displayResult(), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }

    protected abstract List<SlotDisplay> displayIngredients();

    protected abstract SlotDisplay displayResult();

    public static <T extends PNCSpecialCraftingRecipe> RecipeSerializer<T> serializer(Function<CraftingBookCategory, T> factory) {
        return new RecipeSerializer<>(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .xmap(factory, PNCSpecialCraftingRecipe::category),
                CraftingBookCategory.STREAM_CODEC.<T>map(factory, PNCSpecialCraftingRecipe::category).cast()
        );
    }
}
