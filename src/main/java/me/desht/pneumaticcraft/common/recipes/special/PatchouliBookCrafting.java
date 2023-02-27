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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public class PatchouliBookCrafting extends ShapelessRecipe {
    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "pneumaticcraft:book";
    private static Item guide_book;

    public PatchouliBookCrafting(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, "", category, makeGuideBook(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BOOK), Ingredient.of(ModItems.COMPRESSED_IRON_INGOT.get()))
        );
    }

    private static Item guide_book() {
        if (guide_book == null) {
            guide_book = ForgeRegistries.ITEMS.getValue(new ResourceLocation("patchouli:guide_book"));
        }
        return guide_book;
    }

    public static ItemStack makeGuideBook() {
        ItemStack book = new ItemStack(guide_book());
        CompoundTag tag = book.getOrCreateTag();
        tag.putString(NBT_KEY, NBT_VAL);
        return book;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PATCHOULI_BOOK_CRAFTING.get();
    }
}
