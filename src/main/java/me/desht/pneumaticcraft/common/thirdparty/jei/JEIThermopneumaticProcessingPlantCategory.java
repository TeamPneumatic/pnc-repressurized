package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.stream.Collectors;

public class JEIThermopneumaticProcessingPlantCategory extends PneumaticCraftCategory<JEIThermopneumaticProcessingPlantCategory.ThermopneumaticRecipeWrapper> {
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
    public Class<? extends ThermopneumaticRecipeWrapper> getRecipeClass() {
        return ThermopneumaticRecipeWrapper.class;
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

    static Collection<ThermopneumaticRecipeWrapper> getAllRecipes() {
        return PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values().stream()
            .map(ThermopneumaticRecipeWrapper::new)
            .collect(Collectors.toList());
    }

    static class ThermopneumaticRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        ThermopneumaticRecipeWrapper(IThermopneumaticProcessingPlantRecipe recipe) {
            addInputFluid(recipe.getInputFluid(), 8, 4);
            addOutputFluid(recipe.getOutputFluid(), 74, 3);
            if (!recipe.getInputItem().hasNoMatchingItems()) {
                this.addInputItem(PositionedStack.of(recipe.getInputItem().getMatchingStacks(), 41, 3));
            }
            if (recipe.getRequiredPressure() > 0) {
                setUsedPressure(136, 42, recipe.getRequiredPressure(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE);
            }
            setUsedTemperature(92, 12, recipe.getOperatingTemperature().getMin()); // TODO support max temperature
        }
    }
}
