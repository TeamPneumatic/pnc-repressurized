package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WrappedBuilderResult implements IFinishedRecipe {
    private final IFinishedRecipe wrapped;
    private final Supplier<? extends IRecipeSerializer<?>> serializer;

    public WrappedBuilderResult(IFinishedRecipe wrapped, Supplier<? extends IRecipeSerializer<?>> serializer) {
        this.wrapped = wrapped;
        this.serializer = serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        wrapped.serializeRecipeData(json);
    }

    @Override
    public ResourceLocation getId() {
        return wrapped.getId();
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return serializer.get();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return wrapped.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return wrapped.getAdvancementId();
    }
}
