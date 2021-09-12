package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JEIExplosionCraftingCategory implements IRecipeCategory<ExplosionCraftingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIExplosionCraftingCategory() {
        localizedName = I18n.get("pneumaticcraft.gui.nei.title.explosionCrafting");
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
    public Class<? extends ExplosionCraftingRecipe> getRecipeClass() {
        return ExplosionCraftingRecipe.class;
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
    public void setIngredients(ExplosionCraftingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ExplosionCraftingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(ExplosionCraftingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        icon.draw(matrixStack, 30, 0);
    }

    @Override
    public List<ITextComponent> getTooltipStrings(ExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.nei.recipe.explosionCrafting", recipe.getLossRate())));
        }
        return res;
    }
}
