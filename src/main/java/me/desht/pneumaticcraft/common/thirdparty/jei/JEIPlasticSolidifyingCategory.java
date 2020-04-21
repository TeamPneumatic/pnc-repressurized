package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JEIPlasticSolidifyingCategory implements IRecipeCategory<JEIPlasticSolidifyingCategory.PlasticSolidifyingRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIPlasticSolidifyingCategory() {
        localizedName = I18n.format("gui.jei.title.plasticSolidifying");
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.PLASTIC.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.PLASTIC_SOLIDIFYING;
    }

    @Override
    public Class<PlasticSolidifyingRecipe> getRecipeClass() {
        return PlasticSolidifyingRecipe.class;
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
    public void setIngredients(PlasticSolidifyingRecipe recipe, IIngredients ingredients) {
        if (recipe.input instanceof FluidIngredient) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(((FluidIngredient)recipe.input).getFluidStacks()));
        } else {
            ingredients.setInputIngredients(Collections.singletonList(recipe.input));
        }
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PlasticSolidifyingRecipe recipe, IIngredients ingredients) {
        if (recipe.input instanceof FluidIngredient) {
            recipeLayout.getFluidStacks().init(0, true, 1, 1);
            recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        } else {
            recipeLayout.getItemStacks().init(0, true, 0, 0);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        }
        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public List<String> getTooltipStrings(PlasticSolidifyingRecipe recipe, double mouseX, double mouseY) {
        List<String> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.add(I18n.format("gui.jei.tooltip.plasticSolidifying"));
        }
        return res;
    }

    public static Collection<PlasticSolidifyingRecipe> getAllRecipes() {
        return ImmutableList.of(
                new PlasticSolidifyingRecipe(
                        FluidIngredient.of(ModFluids.PLASTIC.get(), 1000),
                        new ItemStack(ModItems.PLASTIC.get())
                ),
                new PlasticSolidifyingRecipe(
                        Ingredient.fromStacks(new ItemStack(ModItems.PLASTIC_BUCKET.get())),
                        new ItemStack(ModItems.PLASTIC.get())
                )
        );
    }

    static class PlasticSolidifyingRecipe {
        final Ingredient input;
        final ItemStack output;

        PlasticSolidifyingRecipe(Ingredient input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }
}
