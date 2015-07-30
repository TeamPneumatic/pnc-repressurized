package pneumaticCraft.common.thirdparty.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.client.gui.GuiThermopneumaticProcessingPlant;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.Textures;
import codechicken.nei.PositionedStack;

public class NEIThermopneumaticProcessingPlantManager extends PneumaticCraftPlugins{

    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Blockss.thermopneumaticProcessingPlant.getUnlocalizedName() + ".name");
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public void drawBackground(int recipe){
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 5, 11, 166, 70);
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return GuiThermopneumaticProcessingPlant.class;
    }

    @Override
    public void loadTransferRects(){
        addTransferRect(new Rectangle(25, 20, 48, 22));
    }

    /*   
       private boolean tankClick(GuiRecipe gui, int recipe, boolean usage){
           Point pos = getMousePosition();
           Point offset = gui.getRecipePosition(recipe);
           Point relMouse = new Point(pos.x - gui.guiLeft - offsetx, pos.y - gui.guiTop - offsety);
       }*/

    private class ThermoNEIRecipe extends MultipleInputOutputRecipe{
        private ThermoNEIRecipe(BasicThermopneumaticProcessingPlantRecipe recipe){
            addInputLiquid(recipe.getInputLiquid(), 8, 4);
            addOutputLiquid(recipe.getOutputLiquid(), 74, 4);
            if(recipe.getInputItem() != null) this.addIngredient(new PositionedStack(recipe.getInputItem(), 41, 3));
            setUsedPressure(136, 42, recipe.getRequiredPressure(null, null));
            setUsedTemperature(92, 12, recipe.getRequiredTemperature(null, null));
        }
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        for(IThermopneumaticProcessingPlantRecipe recipe : PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes) {
            if(recipe instanceof BasicThermopneumaticProcessingPlantRecipe) recipes.add(new ThermoNEIRecipe((BasicThermopneumaticProcessingPlantRecipe)recipe));
        }
        return recipes;
    }

    @Override
    public void drawExtras(int recipe){
        this.drawProgressBar(25, 20, 176, 0, 48, 22, cycleticks % 48 / 48F, 0);
        super.drawExtras(recipe);
    }

}
