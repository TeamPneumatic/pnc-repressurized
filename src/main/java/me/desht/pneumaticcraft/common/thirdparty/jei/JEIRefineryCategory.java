package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class JEIRefineryCategory extends JEIPneumaticCraftCategory<IRefineryRecipe> {
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
    public Class<? extends IRefineryRecipe> getRecipeClass() {
        return IRefineryRecipe.class;
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
    public void setIngredients(IRefineryRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.getInput());
        ingredients.setOutputs(VanillaTypes.FLUID, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRefineryRecipe recipe, IIngredients ingredients) {
        recipeLayout.getFluidStacks().init(0, true, 2, 10);
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        for (int i = 0; i < recipe.getOutputs().size(); i++) {
            recipeLayout.getFluidStacks().init(1 + i, false, 89 + i * 20, 22 - i * 4);
            recipeLayout.getFluidStacks().set(1 + i, recipe.getOutputs().get(i));
        }

        setUsedTemperature(26, 18, recipe.getOperatingTemp());
    }
}
