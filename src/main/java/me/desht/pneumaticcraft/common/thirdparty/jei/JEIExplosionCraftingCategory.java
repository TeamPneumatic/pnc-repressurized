package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JEIExplosionCraftingCategory extends JEIPneumaticCraftCategory<IExplosionCraftingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIExplosionCraftingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        localizedName = I18n.format("gui.nei.title.explosionCrafting");
        background = new ResourceDrawable(Textures.GUI_JEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
        icon = jeiHelpers.getGuiHelper()
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
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInput().getMatchingStacks())));
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IExplosionCraftingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 41, 1);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        for (int i = 0; i < ingredients.getOutputs(VanillaTypes.ITEM).size(); i++) {
            recipeLayout.getItemStacks().init(1 + i , false, 105 + (i * 18), 1);
            recipeLayout.getItemStacks().set(1 + i, ingredients.getOutputs(VanillaTypes.ITEM).get(i));
        }
    }

    @Override
    public List<String> getTooltipStrings(IExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        List<String> l = super.getTooltipStrings(recipe, mouseX, mouseY);
        if (mouseX >= 63 && mouseX <= 100) {
            l.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.nei.recipe.explosionCrafting", recipe.getLossRate()), 32));
        }
        return l;
    }

    @Override
    public void draw(IExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);

        drawIconAt(icon, 73, 0);
    }
}
