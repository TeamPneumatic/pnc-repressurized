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
import me.desht.pneumaticcraft.common.item.minigun.StandardGunAmmoItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class GunAmmoPotionCrafting extends ShapelessRecipe {
    public GunAmmoPotionCrafting(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, "", category, new ItemStack(ModItems.GUN_AMMO.get()),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.GUN_AMMO.get()), new PotionIngredient()));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PotionItem) {
                potion = stack;
            } else if (stack.getItem() == ModItems.GUN_AMMO.get()) {
                ammo = stack;
            } else if (!stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        if (ammo.isEmpty() || potion.isEmpty()) return ItemStack.EMPTY;

        ammo = ammo.copy();
        StandardGunAmmoItem.setPotion(ammo, potion);
        return ammo;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GUN_AMMO_POTION_CRAFTING.get();
    }

    private static class PotionIngredient extends Ingredient {
        PotionIngredient() {
            super(Stream.empty());
        }

        @Override
        public ItemStack[] getItems() {
            NonNullList<ItemStack> potions = NonNullList.create();
            for (Potion p : ForgeRegistries.POTIONS.getValues()) {
                if (p != Potions.EMPTY) potions.add(PotionUtils.setPotion(new ItemStack(Items.POTION), p));
            }

            return potions.toArray(new ItemStack[0]);
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            return !PotionUtils.getMobEffects(stack).isEmpty();
        }
    }
}
