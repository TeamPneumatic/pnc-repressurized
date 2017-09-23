package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import net.minecraft.client.resources.I18n;

public class JEIThermopneumaticProcessingPlantCategory extends PneumaticCraftCategory<JEIThermopneumaticProcessingPlantCategory.ThermopneumaticRecipeWrapper> {

    public JEIThermopneumaticProcessingPlantCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.THERMO_PNEUMATIC;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.THERMOPNEUMATIC_PROCESSING_PLANT.getUnlocalizedName() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 0, 0, 5, 11, 166, 70);
    }

    static class ThermopneumaticRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        ThermopneumaticRecipeWrapper(BasicThermopneumaticProcessingPlantRecipe recipe) {
            addInputLiquid(recipe.getInputLiquid(), 8, 4);
            addOutputLiquid(recipe.getOutputLiquid(), 74, 3);
            if (recipe.getInputItem() != null) this.addIngredient(new PositionedStack(recipe.getInputItem(), 41, 3));
            setUsedPressure(136, 42, recipe.getRequiredPressure(null, null));
            setUsedTemperature(92, 12, recipe.getRequiredTemperature(null, null));
        }
    }

    /*  @Override
      public void drawExtras(int recipe){
          this.drawProgressBar(25, 20, 176, 0, 48, 22, cycleticks % 48 / 48F, 0);
          super.drawExtras(recipe);
      }*/

//    @Override
//    public Class<BasicThermopneumaticProcessingPlantRecipe> getRecipeClass() {
//        return BasicThermopneumaticProcessingPlantRecipe.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(BasicThermopneumaticProcessingPlantRecipe recipe) {
//        return new ThermoNEIRecipeWrapper(recipe);
//    }
//
//    @Override
//    public boolean isRecipeValid(BasicThermopneumaticProcessingPlantRecipe recipe) {
//        return true;
//    }

}
