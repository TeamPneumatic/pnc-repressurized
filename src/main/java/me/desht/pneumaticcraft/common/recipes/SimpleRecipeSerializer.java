package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Function<ResourceLocation, T> constructor;

    public SimpleRecipeSerializer(Function<ResourceLocation, T> pConstructor) {
        this.constructor = pConstructor;
    }

    public T fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
        return this.constructor.apply(pRecipeId);
    }

    public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        return this.constructor.apply(pRecipeId);
    }

    public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
    }
}
