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

import com.google.common.base.Suppliers;
import me.desht.pneumaticcraft.common.item.minigun.StandardGunAmmoItem;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GunAmmoPotionCrafting extends ShapelessRecipe {
    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof PotionItem,
            stack -> stack.getItem() instanceof StandardGunAmmoItem
    );
    private static final Supplier<Ingredient> POTIONS = Suppliers.memoize(() ->
        Ingredient.of(BuiltInRegistries.POTION.holders().flatMap(GunAmmoPotionCrafting::potionStacks))
    );

    public GunAmmoPotionCrafting(CraftingBookCategory category) {
        super("", category, ModItems.GUN_AMMO.get().getDefaultInstance(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.GUN_AMMO.get()), POTIONS.get())
        );
    }

    private static Stream<ItemStack> potionStacks(Holder<Potion> potion) {
        return Stream.of(
                PotionContents.createItemStack(Items.POTION, potion),
                PotionContents.createItemStack(Items.SPLASH_POTION, potion),
                PotionContents.createItemStack(Items.LINGERING_POTION, potion)
        );
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
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
}
