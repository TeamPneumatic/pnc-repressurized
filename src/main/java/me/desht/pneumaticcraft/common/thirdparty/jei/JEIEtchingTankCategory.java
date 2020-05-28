package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;

public class JEIEtchingTankCategory implements IRecipeCategory<JEIEtchingTankCategory.EtchingTankRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progressBar;

    JEIEtchingTankCategory() {
        localizedName = I18n.format(ModBlocks.ETCHING_TANK.get().getTranslationKey());
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 0, 0, 83, 42);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.ETCHING_TANK.get()));
        IDrawableStatic d = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 83, 0, 42, 42);
        progressBar = JEIPlugin.jeiHelpers.getGuiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    public static Collection<?> getAllRecipes() {
        ItemStack[] input = new ItemStack[4];
        for (int i = 0; i < input.length; i++) {
            input[i] = new ItemStack(ModItems.EMPTY_PCB.get());
            TileEntityUVLightBox.setExposureProgress(input[i], 25 + 25 * i);
        }

        return Collections.singletonList(
                new EtchingTankRecipe(
                        Ingredient.fromStacks(input),
                        new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                        new ItemStack(ModItems.FAILED_PCB.get())
                )
        );
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.ETCHING_TANK;
    }

    @Override
    public Class<? extends EtchingTankRecipe> getRecipeClass() {
        return EtchingTankRecipe.class;
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
    public void setIngredients(EtchingTankRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.input));
        ingredients.setInput(VanillaTypes.FLUID, new FluidStack(ModFluids.ETCHING_ACID.get(), 1000));
        ingredients.setOutputs(VanillaTypes.ITEM, ImmutableList.of(recipe.output, recipe.failed));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, EtchingTankRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 12);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getFluidStacks().init(1, true, 26, 13);
        recipeLayout.getFluidStacks().set(1, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        recipeLayout.getItemStacks().init(2, false, 65, 0);
        recipeLayout.getItemStacks().set(2, ingredients.getOutputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(3, false, 65, 24);
        recipeLayout.getItemStacks().set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(1));
    }

    @Override
    public void draw(EtchingTankRecipe recipe, double mouseX, double mouseY) {
        progressBar.draw(20, 0);
    }

    static class EtchingTankRecipe {
        final Ingredient input;
        final ItemStack output;
        final ItemStack failed;

        EtchingTankRecipe(Ingredient input, ItemStack output, ItemStack failed) {
            this.input = input;
            this.output = output;
            this.failed = failed;
        }
    }
}
