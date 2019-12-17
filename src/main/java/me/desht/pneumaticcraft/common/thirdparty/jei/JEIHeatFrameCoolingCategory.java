package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class JEIHeatFrameCoolingCategory extends PneumaticCraftCategory<JEIHeatFrameCoolingCategory.HeatFrameCoolingRecipeWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIHeatFrameCoolingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        localizedName = I18n.format("gui.nei.title.heatFrameCooling");
        background = new ResourceDrawable(Textures.GUI_JEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.HEAT_FRAME));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.HEAT_FRAME_COOLING;
    }

    @Override
    public Class<? extends JEIHeatFrameCoolingCategory.HeatFrameCoolingRecipeWrapper> getRecipeClass() {
        return HeatFrameCoolingRecipeWrapper.class;
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

    static List<HeatFrameCoolingRecipeWrapper> getAllRecipes() {
        return PneumaticCraftRecipes.heatFrameCoolingRecipes.values().stream()
                .map(HeatFrameCoolingRecipeWrapper::new)
                .collect(Collectors.toList());
    }

    static class HeatFrameCoolingRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        HeatFrameCoolingRecipeWrapper(IHeatFrameCoolingRecipe recipe) {
            this.addInputItem(PositionedStack.of(recipe.getInput().getMatchingStacks(), 41, 1));
            this.addOutputItem(PositionedStack.of(recipe.getOutput(), 105, 1));
        }

        @Override
        public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
            super.drawInfo(recipeWidth, recipeHeight, mouseX, mouseY);

            drawTextAt("gui.nei.recipe.heatFrameCooling", 5, 24);
        }
    }
}
