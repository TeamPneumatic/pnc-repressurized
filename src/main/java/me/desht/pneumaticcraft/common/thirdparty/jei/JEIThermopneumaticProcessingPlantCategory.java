package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;

public class JEIThermopneumaticProcessingPlantCategory extends JEIPneumaticCraftCategory<IThermopneumaticProcessingPlantRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIThermopneumaticProcessingPlantCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT));
        background = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 5, 11, 166, 70);
        localizedName = I18n.format(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.getTranslationKey());
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.THERMO_PNEUMATIC;
    }

    @Override
    public Class<? extends IThermopneumaticProcessingPlantRecipe> getRecipeClass() {
        return IThermopneumaticProcessingPlantRecipe.class;
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
    public void setIngredients(IThermopneumaticProcessingPlantRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.getInputFluid());
        if (recipe.getInputItem() != null) {
            ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInputItem().getMatchingStacks())));
        }
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputFluid());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IThermopneumaticProcessingPlantRecipe recipe, IIngredients ingredients) {
        super.setRecipe(recipeLayout, recipe, ingredients);

        if (recipe.getInputItem() != null) {
            recipeLayout.getItemStacks().init(0, true, 41, 3);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        }
        recipeLayout.getFluidStacks().init(0, true, 8, 4);
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        recipeLayout.getFluidStacks().init(1, false, 75, 3);
        recipeLayout.getFluidStacks().set(1, ingredients.getOutputs(VanillaTypes.FLUID).get(0));

        if (recipe.getRequiredPressure() > 0) {
            setUsedPressure(136, 42, recipe.getRequiredPressure(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE);
        }
        setUsedTemperature(92, 12, recipe.getOperatingTemperature());
    }
}
