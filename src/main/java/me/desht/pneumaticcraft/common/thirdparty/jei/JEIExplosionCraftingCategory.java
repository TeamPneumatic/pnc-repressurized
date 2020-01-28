package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JEIExplosionCraftingCategory implements IRecipeCategory<IExplosionCraftingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIExplosionCraftingCategory() {
        localizedName = I18n.format("gui.nei.title.explosionCrafting");
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18);
        icon = JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.JEI_EXPLOSION, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.EXPLOSION_CRAFTING;
    }

    @Override
    public Class<? extends IExplosionCraftingRecipe> getRecipeClass() {
        return IExplosionCraftingRecipe.class;
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
    public void setIngredients(IExplosionCraftingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IExplosionCraftingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(IExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        Helpers.drawIconAt(icon,30, 0);
    }

    @Override
    public List<String> getTooltipStrings(IExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitString(I18n.format("gui.nei.recipe.explosionCrafting", recipe.getLossRate()), 32));
        }
        return res;
    }

    static Collection<IExplosionCraftingRecipe> getAllRecipes() {
        return PneumaticCraftRecipes.explosionCraftingRecipes.values();
    }
}
