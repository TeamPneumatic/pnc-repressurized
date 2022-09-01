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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

import java.util.*;

/**
 * Cached vanilla crafting table recipe lookup, primarily for the benefit of drone crafting widget.
 */
public class RecipeCache<T extends IRecipeType<R>, R extends IRecipe<C>, C extends IInventory> {
    public static final RecipeCache<IRecipeType<ICraftingRecipe>, ICraftingRecipe, CraftingInventory> CRAFTING = new RecipeCache<>(IRecipeType.CRAFTING);
    public static final RecipeCache<IRecipeType<FurnaceRecipe>, FurnaceRecipe, IInventory> SMELTING = new RecipeCache<>(IRecipeType.SMELTING);

    private static final int MAX_CACHE_SIZE = 1024;

    private final T type;
    private final Int2ObjectLinkedOpenHashMap<Optional<R>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

    private RecipeCache(T type) {
        this.type = type;
    }

    public Optional<R> getCachedRecipe(World world, C inv) {
        int key = makeKey(inv);
        if (recipeCache.containsKey(key)) {
            return recipeCache.getAndMoveToFirst(key);
        } else {
            Optional<R> newRecipe = world.getRecipeManager().getRecipeFor(type, inv, world);
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

    private int makeKey(IInventory inv) {
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
