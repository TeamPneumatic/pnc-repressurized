package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;

public class JEIHeatFrameCoolingCategory extends JEIPneumaticCraftCategory<IHeatFrameCoolingRecipe> {
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
    public Class<? extends IHeatFrameCoolingRecipe> getRecipeClass() {
        return IHeatFrameCoolingRecipe.class;
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

    @Override
    public void draw(IHeatFrameCoolingRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);

        drawTextAt("gui.nei.recipe.heatFrameCooling", 5, 24);
    }

    @Override
    public void setIngredients(IHeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInput().getMatchingStacks())));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IHeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 41, 1);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 105, 1);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }
}
