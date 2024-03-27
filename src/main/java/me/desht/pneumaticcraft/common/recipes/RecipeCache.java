package me.desht.pneumaticcraft.common.recipes;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class RecipeCache<R extends Recipe<?>> {
    private static final int MAX_CACHE_SIZE = 1024;

    private final Int2ObjectLinkedOpenHashMap<Optional<RecipeHolder<R>>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

    public Optional<RecipeHolder<R>> getCachedRecipe(Supplier<Optional<RecipeHolder<R>>> recipeFinder, IntSupplier hashCodeGenerator) {
        int key = hashCodeGenerator.getAsInt();

        if (recipeCache.containsKey(key)) {
            return recipeCache.getAndMoveToFirst(key);
        } else {
            Optional<RecipeHolder<R>> newRecipe = recipeFinder.get();
            while (recipeCache.size() >= MAX_CACHE_SIZE) {
                recipeCache.removeLast();
            }
            recipeCache.put(key, newRecipe);
            return newRecipe;
        }
    }

    void clear() {
        recipeCache.clear();
    }
}
