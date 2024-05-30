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

import me.desht.pneumaticcraft.common.item.minigun.StandardGunAmmoItem;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GunAmmoPotionCrafting extends ShapelessRecipe {
    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof PotionItem,
            stack -> stack.getItem() instanceof StandardGunAmmoItem
    );

    public GunAmmoPotionCrafting(CraftingBookCategory category) {
        super("", category, ModItems.GUN_AMMO.get().getDefaultInstance(), NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.GUN_AMMO.get()), new PotionIngredient())
        );
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(inv, ITEM_PREDICATES);
        if (stacks.size() == 2) {
            ItemStack potion = stacks.get(0);
            ItemStack ammo = stacks.get(1).copy();
            if (!ammo.isEmpty() && !potion.isEmpty()) {
                StandardGunAmmoItem.setPotion(ammo, potion);
                return ammo;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
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
            for (Potion p : BuiltInRegistries.POTION) {
                if (p != Potions.EMPTY) potions.add(PotionUtils.setPotion(new ItemStack(Items.POTION), p));
            }

            return potions.toArray(new ItemStack[0]);
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            return stack != null && !PotionUtils.getMobEffects(stack).isEmpty();
        }
    }
}
