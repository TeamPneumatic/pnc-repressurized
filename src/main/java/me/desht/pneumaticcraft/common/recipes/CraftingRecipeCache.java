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

package me.desht.pneumaticcraft.common.recipes;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Cached vanilla crafting table recipe lookup, primarily for the benefit of drone crafting widget.
 */
public enum CraftingRecipeCache {
    INSTANCE;

    private static final int MAX_CACHE_SIZE = 1024;

    private final Int2ObjectLinkedOpenHashMap<Optional<CraftingRecipe>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

    public Optional<CraftingRecipe> getCachedRecipe(Level world, CraftingContainer inv) {
        int key = makeKey(inv);
        if (recipeCache.containsKey(key)) {
            return recipeCache.getAndMoveToFirst(key);
        } else {
            Optional<CraftingRecipe> newRecipe = world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, world);
            if (recipeCache.size() == MAX_CACHE_SIZE) {
                recipeCache.removeLast();
            }
            recipeCache.put(key, newRecipe);
            return newRecipe;
        }
    }

    public void clear() {
        recipeCache.clear();
    }

    private int makeKey(CraftingContainer inv) {
        List<Integer> c = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                c.add(i);
                c.add(stack.getItem().hashCode());
                if (stack.hasTag()) c.add(Objects.requireNonNull(stack.getTag()).hashCode());
            }
        }
        return Arrays.hashCode(c.toArray(new Integer[0]));
    }
}
