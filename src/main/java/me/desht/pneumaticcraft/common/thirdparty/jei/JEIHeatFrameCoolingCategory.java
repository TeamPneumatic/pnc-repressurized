package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JEIHeatFrameCoolingCategory implements IRecipeCategory<IHeatFrameCoolingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIHeatFrameCoolingCategory() {
        localizedName = I18n.format("gui.nei.title.heatFrameCooling");
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.HEAT_FRAME.get()));
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
    public void setIngredients(IHeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IHeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, recipe.getOutput());
    }

    @Override
    public List<String> getTooltipStrings(IHeatFrameCoolingRecipe recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.nei.recipe.heatFrameCooling", recipe.getTemperature() - 273), 32));
        }
        return res;
    }

    static Collection<IHeatFrameCoolingRecipe> getAllRecipes() {
        return PneumaticCraftRecipes.heatFrameCoolingRecipes.values();
    }
}
