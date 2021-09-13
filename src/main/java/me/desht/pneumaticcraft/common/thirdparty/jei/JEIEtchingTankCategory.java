package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIEtchingTankCategory extends AbstractPNCCategory<JEIEtchingTankCategory.EtchingTankRecipe> {
    private final IDrawableAnimated progressBar;

    JEIEtchingTankCategory() {
        super(ModCategoryUid.ETCHING_TANK, EtchingTankRecipe.class,
                xlate(ModBlocks.ETCHING_TANK.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 0, 0, 83, 42),
                guiHelper().createDrawableIngredient(new ItemStack(ModBlocks.ETCHING_TANK.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 83, 0, 42, 42);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
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
    public void draw(EtchingTankRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        progressBar.draw(matrixStack, 20, 0);
    }

    static Collection<?> getAllRecipes() {
        ItemStack[] input = new ItemStack[4];
        for (int i = 0; i < input.length; i++) {
            input[i] = new ItemStack(ModItems.EMPTY_PCB.get());
            TileEntityUVLightBox.setExposureProgress(input[i], 25 + 25 * i);
        }

        return Collections.singletonList(
                new EtchingTankRecipe(
                        Ingredient.of(input),
                        new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                        new ItemStack(ModItems.FAILED_PCB.get())
                )
        );
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
