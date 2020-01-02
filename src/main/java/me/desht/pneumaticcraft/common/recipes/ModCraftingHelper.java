package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.IModRecipeSerializer;
import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModCraftingHelper {
    private static final Map<ResourceLocation, Supplier<IModRecipeSerializer<? extends IModRecipe>>> factories = new HashMap<>();

    public static void registerSerializer(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> deserializer) {
        factories.put(recipeType, deserializer);
    }

    public static <T extends IModRecipe> IModRecipeSerializer<T> getSerializer(ResourceLocation type) {
        //noinspection unchecked
        return (IModRecipeSerializer<T>) factories.get(type).get();
    }

    public static <T extends IModRecipe> IModRecipeSerializer<T> getSerializer(T recipe) {
        //noinspection unchecked
        return (IModRecipeSerializer<T>) factories.get(recipe.getRecipeType()).get();
    }

    public static void writeRecipe(IModRecipe recipe, PacketBuffer buf) {
        getSerializer(recipe).write(buf, recipe);
    }

    public static <T extends IModRecipe> T readRecipe(PacketBuffer buf) {
        //noinspection unchecked
        return (T) getSerializer(buf.readResourceLocation()).read(buf.readResourceLocation(), buf);
    }
}
