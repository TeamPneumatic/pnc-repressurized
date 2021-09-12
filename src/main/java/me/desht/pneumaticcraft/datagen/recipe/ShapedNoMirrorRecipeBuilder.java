package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class ShapedNoMirrorRecipeBuilder extends ShapedRecipeBuilder {
    public ShapedNoMirrorRecipeBuilder(IItemProvider resultIn, int countIn) {
        super(resultIn, countIn);
    }

    public static ShapedNoMirrorRecipeBuilder shapedRecipe(IItemProvider resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static ShapedNoMirrorRecipeBuilder shapedRecipe(IItemProvider resultIn, int countIn) {
        return new ShapedNoMirrorRecipeBuilder(resultIn, countIn);
    }

    public void save(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
        Consumer<IFinishedRecipe> c = (finishedRecipe) -> consumerIn.accept(new WrappedBuilderResult(finishedRecipe, ModRecipes.CRAFTING_SHAPED_NO_MIRROR));
        super.save(c, id);
    }
}
