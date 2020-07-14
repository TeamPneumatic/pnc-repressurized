package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JEIHeatFrameCoolingCategory implements IRecipeCategory<HeatFrameCoolingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable bonusIcon;
    private final IDrawableAnimated progressBar;

    JEIHeatFrameCoolingCategory() {
        localizedName = I18n.format("pneumaticcraft.gui.nei.title.heatFrameCooling");
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.HEAT_FRAME.get()));
        IDrawableStatic d = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 82, 0, 38, 17);
        progressBar = JEIPlugin.jeiHelpers.getGuiHelper().createAnimatedDrawable(d, 30, IDrawableAnimated.StartDirection.LEFT, false);
        bonusIcon = JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.GUI_JEI_BONUS, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.HEAT_FRAME_COOLING;
    }

    @Override
    public Class<? extends HeatFrameCoolingRecipe> getRecipeClass() {
        return HeatFrameCoolingRecipe.class;
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
    public void draw(HeatFrameCoolingRecipe recipe, double mouseX, double mouseY) {
        progressBar.draw(22, 0);
        if (recipe.getBonusMultiplier() > 0f) {
            bonusIcon.draw(30, 0);
        }
    }

    @Override
    public void setIngredients(HeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, HeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, recipe.getOutput());
    }

    @Override
    public List<String> getTooltipStrings(HeatFrameCoolingRecipe recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.nei.recipe.heatFrameCooling", recipe.getThresholdTemperature() - 273), 40));
            if (recipe.getBonusMultiplier() > 0f) {
                String bonus = TextFormatting.YELLOW + I18n.format("pneumaticcraft.gui.nei.recipe.heatFrameCooling.bonus", recipe.getBonusMultiplier() * 100, recipe.getOutput().getDisplayName().getFormattedText(), recipe.getThresholdTemperature() - 273, recipe.getBonusLimit() + 1);
                res.addAll(PneumaticCraftUtils.splitString(bonus, 40));
            }
        }
        return res;
    }
}
