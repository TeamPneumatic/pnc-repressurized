package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.stream.Collectors;

public class JEIRefineryCategory extends PneumaticCraftCategory<JEIRefineryCategory.RefineryRecipeWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIRefineryCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.REFINERY));
        background = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_REFINERY, 6, 3, 166, 79);
        localizedName = I18n.format(ModBlocks.REFINERY.getTranslationKey());
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.REFINERY;
    }

    @Override
    public Class<? extends RefineryRecipeWrapper> getRecipeClass() {
        return RefineryRecipeWrapper.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    static Collection<RefineryRecipeWrapper> getAllRecipes() {
        return PneumaticCraftRecipes.refineryRecipes.values().stream()
                .map(RefineryRecipeWrapper::new)
                .collect(Collectors.toList());
    }

    static class RefineryRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        final int refineries;

        private RefineryRecipeWrapper(IRefineryRecipe recipe) {
            this.refineries = recipe.getOutputs().size();
            addInputFluid(recipe.getInput(), 2, 10);
            int x = 69;
            int y = 18;
            for (int i = 0; i < recipe.getOutputs().size(); i++) {
                x += 20;
                y -= 4;
                addOutputFluid(recipe.getOutputs().get(i), x, y);
            }
            // TODO support for maximum temp
            setUsedTemperature(26, 18, recipe.getOperatingTemp().getMin()); //getMinimumTemp());
        }
    }
}
