package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.common.thirdparty.jei.JEIRefineryCategory.RefineryRecipeWrapper;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class JEIRefineryCategory extends PneumaticCraftCategory<RefineryRecipeWrapper> {

    JEIRefineryCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.REFINERY;
    }

    @Override
    public String getTitle() {
        return I18n.format(ModBlocks.REFINERY.getTranslationKey() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_REFINERY, 0, 0, 6, 3, 166, 79);
    }

    static class RefineryRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        final int refineries;

        private RefineryRecipeWrapper(RefineryRecipe recipe) {
            this.refineries = recipe.outputs.length;
            addInputLiquid(recipe.input, 2, 10);
            int x = 69;
            int y = 18;
            for (int i = 0; i < recipe.outputs.length; i++) {
                x += 20;
                y -= 4;
                addOutputLiquid(recipe.outputs[i], x, y);
            }
            setUsedTemperature(26, 18, recipe.getMinimumTemp());
        }

    }

    List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        for (RefineryRecipe recipe : RefineryRecipe.recipes) {
        	recipes.add(new RefineryRecipeWrapper(recipe));
        }
        return recipes;
    }
}
