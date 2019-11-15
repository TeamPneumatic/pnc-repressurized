package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.UVLightBoxRecipe;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class JEIUVLightBoxCategory extends JEIPneumaticCraftCategory<UVLightBoxRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    static final List<UVLightBoxRecipe> UV_LIGHT_BOX_RECIPES;
    static {
        ItemStack in = new ItemStack(ModItems.EMPTY_PCB);
        in.setDamage(in.getMaxDamage());
        ItemStack out = new ItemStack(ModItems.EMPTY_PCB);
        UVLightBoxRecipe recipe = new UVLightBoxRecipe(in, out);
        UV_LIGHT_BOX_RECIPES = Collections.singletonList(recipe);
    }

    JEIUVLightBoxCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        localizedName = I18n.format(ModBlocks.UV_LIGHT_BOX.getTranslationKey());
        background = new ResourceDrawable(Textures.GUI_JEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.UV_LIGHT_BOX));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.UV_LIGHT_BOX;
    }

    @Override
    public Class<? extends UVLightBoxRecipe> getRecipeClass() {
        return UVLightBoxRecipe.class;
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
    public void setIngredients(UVLightBoxRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.getIn());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOut());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, UVLightBoxRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 41, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 105, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(UVLightBoxRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);

        drawIconAt(icon, 73, -2);

        drawTextAt("gui.nei.recipe.uvLightBox", 5, 24);
    }
}
