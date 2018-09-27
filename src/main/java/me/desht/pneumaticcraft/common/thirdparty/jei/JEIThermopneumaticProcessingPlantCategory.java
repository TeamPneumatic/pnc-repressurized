package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class JEIThermopneumaticProcessingPlantCategory extends PneumaticCraftCategory<JEIThermopneumaticProcessingPlantCategory.ThermopneumaticRecipeWrapper> {

    JEIThermopneumaticProcessingPlantCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.THERMO_PNEUMATIC;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.THERMOPNEUMATIC_PROCESSING_PLANT.getTranslationKey() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 0, 0, 5, 11, 166, 70);
    }

    static class ThermopneumaticRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        ThermopneumaticRecipeWrapper(BasicThermopneumaticProcessingPlantRecipe recipe) {
            addInputLiquid(recipe.getInputLiquid(), 8, 4);
            addOutputLiquid(recipe.getOutputLiquid(), 74, 3);
            if (!recipe.getInputItem().isEmpty()) this.addIngredient(new PositionedStack(recipe.getInputItem(), 41, 3));
            if (recipe.getRequiredPressure(recipe.getInputLiquid(), recipe.getInputItem()) > 0) {
                setUsedPressure(136, 42, recipe.getRequiredPressure(null, ItemStack.EMPTY), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE);
            }
            setUsedTemperature(92, 12, recipe.getRequiredTemperature(null, ItemStack.EMPTY));
        }
    }
}
