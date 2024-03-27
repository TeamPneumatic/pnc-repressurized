package me.desht.pneumaticcraft.common.recipes;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Supplier<T> constructor;
    private final Codec<T> codec;

    public SimpleRecipeSerializer(Supplier<T> pConstructor) {
        this.constructor = pConstructor;
        this.codec = Codec.unit(constructor);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public T fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return constructor.get();
    }

    public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
    }
}
