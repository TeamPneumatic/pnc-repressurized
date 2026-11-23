package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.function.Function;

/**
 * A shaped recipe which wraps a regular {@link ShapedRecipe} (same JSON format) and modifies its result.
 */
public abstract class WrappedShapedRecipe extends ShapedRecipe {
    protected final ShapedRecipe wrapped;

    public WrappedShapedRecipe(ShapedRecipe wrapped) {
        super(new Recipe.CommonInfo(wrapped.showNotification()),
                new CraftingRecipe.CraftingBookInfo(wrapped.category(), wrapped.group()),
                wrapped.pattern,
                ((ShapedRecipeAccess) wrapped).getResult());

        this.wrapped = wrapped;
    }

    public ShapedRecipe wrapped() {
        return wrapped;
    }

    protected abstract RecipeSerializer<? extends WrappedShapedRecipe> wrappedSerializer();

    /**
     * {@link ShapedRecipe#getSerializer()} is typed to {@code RecipeSerializer<ShapedRecipe>}, so subclasses can't
     * return their own serializer from it directly. The serializer is only used to dispatch the codec, and it does
     * handle this recipe, so the unchecked cast is safe.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) wrappedSerializer();
    }

    protected static <T extends WrappedShapedRecipe> RecipeSerializer<T> serializer(Function<ShapedRecipe, T> factory) {
        return new RecipeSerializer<>(
                ShapedRecipe.MAP_CODEC.xmap(factory, WrappedShapedRecipe::wrapped),
                ShapedRecipe.STREAM_CODEC.map(factory, WrappedShapedRecipe::wrapped)
        );
    }
}
