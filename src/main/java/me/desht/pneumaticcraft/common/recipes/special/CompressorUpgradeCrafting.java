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

import cofh.lib.util.constants.NBTTags;
import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.DummyRegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;


public class CompressorUpgradeCrafting extends ShapedRecipe {

	public static final Serializer SERIALIZER = new Serializer();
	public static final Supplier<List<Item>> COMPRESSORS = Suppliers.memoize(() -> List.of(
			getItem("advanced_air_compressor"),
			getItem("advanced_liquid_compressor"),
			getItem("air_compressor"),
			getItem("liquid_compressor")
	));

	public CompressorUpgradeCrafting(ResourceLocation pId, String pGroup, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult) {
		super(pId, pGroup, CraftingBookCategory.MISC, pWidth, pHeight, pRecipeItems, pResult);
	}

	private static Item getItem(String id) {
		return ForgeRegistries.ITEMS.getValue(RL(id));
	}

	@NotNull
	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack result = super.assemble(inv, registryAccess);

		int index = getMainItem(inv);
		if (index == -1) {
			Log.warn("Just crafted a PNC Compressor upgrade recipe but couldn't find a compressor in the input!");
			return result;
		}
		ItemStack input = inv.getItem(index);

		CompoundTag tag = input.getTag();
		if (tag == null) return result;

		Tag blockEntityTag = tag.get(NBTTags.TAG_BLOCK_ENTITY);
		if (blockEntityTag == null) return result;

		result.getOrCreateTag().put(NBTTags.TAG_BLOCK_ENTITY, blockEntityTag);
		return result;
	}

	private int getMainItem(CraftingContainer container) {
		int i;
		boolean matchFound = false;

		for (i = 0; i < container.getContainerSize(); i++) {
			ItemStack item = container.getItem(i);
			if (COMPRESSORS.get().contains(item.getItem())) {
				matchFound = true;
				break;
			}
		}
			if (!matchFound) {
			return -1;
		}
		return i;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	static class Serializer extends ShapedRecipe.Serializer {
		@Override
		public ShapedRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
			ShapedRecipe r = super.fromJson(pRecipeId, pJson);
			return new CompressorUpgradeCrafting(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem(DummyRegistryAccess.INSTANCE));
		}

		@Override
		public ShapedRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			ShapedRecipe r = super.fromNetwork(pRecipeId, pBuffer);
			return new CompressorUpgradeCrafting(r.getId(), r.getGroup(), r.getRecipeWidth(), r.getRecipeHeight(), r.getIngredients(), r.getResultItem(DummyRegistryAccess.INSTANCE));
		}
	}
}
