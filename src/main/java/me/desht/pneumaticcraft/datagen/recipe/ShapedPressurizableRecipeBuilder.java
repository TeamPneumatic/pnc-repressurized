package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class ShapedPressurizableRecipeBuilder extends ShapedRecipeBuilder {
    public ShapedPressurizableRecipeBuilder(IItemProvider resultIn, int countIn) {
        super(resultIn, countIn);
    }

    public static ShapedPressurizableRecipeBuilder shapedRecipe(IItemProvider resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static ShapedPressurizableRecipeBuilder shapedRecipe(IItemProvider resultIn, int countIn) {
        return new ShapedPressurizableRecipeBuilder(resultIn, countIn);
    }

    public void save(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
        Consumer<IFinishedRecipe> c = (finishedRecipe) -> consumerIn.accept(new WrappedBuilderResult(finishedRecipe, ModRecipes.CRAFTING_SHAPED_PRESSURIZABLE));
        super.save(c, id);
    }
}
