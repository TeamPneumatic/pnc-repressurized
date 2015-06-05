package pneumaticCraft.common.thirdparty.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.client.gui.GuiThermopneumaticProcessingPlant;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.Textures;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIThermopneumaticProcessingPlantManager extends TemplateRecipeHandler{
    public static final String RECT_RECIPE = "ThermoRect";

    /**
     * In this function you need to fill up the empty recipe array with recipes.
     * The default passes it to a cleaner handler if outputId is an item
     *
     * @param outputId A String identifier representing the type of output produced. Eg. {"item", "fuel"}
     * @param results  Objects representing the results that matching recipes must produce.
     */
    @Override
    public void loadCraftingRecipes(String outputId, Object... results){
        super.loadCraftingRecipes(outputId, results);
        if(outputId.equals(RECT_RECIPE)) loadCraftingRecipes((FluidStack)null);
        if(outputId.equals("liquid") && results.length > 0 && results[0] instanceof FluidStack) loadCraftingRecipes((FluidStack)results[0]);
    }

    /**
     * In this function you need to fill up the empty recipe array with recipes
     * The default passes it to a cleaner handler if inputId is an item
     *
     * @param inputId     A String identifier representing the type of ingredients used. Eg. {"item", "fuel"}
     * @param ingredients Objects representing the ingredients that matching recipes must contain.
     */
    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients){
        super.loadUsageRecipes(inputId, ingredients);
        if(inputId.equals(RECT_RECIPE)) loadUsageRecipes((FluidStack)null);
        if(inputId.equals("liquid") && ingredients.length > 0 && ingredients[0] instanceof FluidStack) loadUsageRecipes((FluidStack)ingredients[0]);
    }

    protected void loadCraftingRecipes(FluidStack stack){
        for(BasicThermopneumaticProcessingPlantRecipe recipe : getBasicRecipes()) {
            if(stack == null || recipe.getOutputLiquid() != null && recipe.getOutputLiquid().getFluid() == stack.getFluid()) {
                arecipes.add(new ThermoNEIRecipe(recipe));
            }
        }
    }

    protected void loadUsageRecipes(FluidStack stack){
        for(BasicThermopneumaticProcessingPlantRecipe recipe : getBasicRecipes()) {
            if(stack == null || recipe.getInputLiquid() != null && recipe.getInputLiquid().getFluid() == stack.getFluid()) {
                arecipes.add(new ThermoNEIRecipe(recipe));
            }
        }
    }

    /**
     * Simplified wrapper, implement this and fill the empty recipe array with recipes
     *
     * @param ingredient The ingredient the recipes must contain.
     */
    @Override
    public void loadUsageRecipes(ItemStack ingredient){
        for(BasicThermopneumaticProcessingPlantRecipe recipe : getBasicRecipes()) {
            if(recipe.getInputItem() != null && ingredient.isItemEqual(recipe.getInputItem())) {
                arecipes.add(new ThermoNEIRecipe(recipe));
            }
        }
    }

    private List<BasicThermopneumaticProcessingPlantRecipe> getBasicRecipes(){
        List<BasicThermopneumaticProcessingPlantRecipe> recipes = new ArrayList<BasicThermopneumaticProcessingPlantRecipe>();
        for(IThermopneumaticProcessingPlantRecipe recipe : PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes) {
            if(recipe instanceof BasicThermopneumaticProcessingPlantRecipe) recipes.add((BasicThermopneumaticProcessingPlantRecipe)recipe);
        }
        return recipes;
    }

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
    public void drawExtras(int recipe){
        ThermoNEIRecipe r = (ThermoNEIRecipe)arecipes.get(recipe);
        // drawAnimatedPressureGauge(120, 27, -1, r.getRequiredPressure(null, null), PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, cycleticks % 48 / 48F);

        r.inputTank.render(0, 0, 0);
        r.outputTank.render(0, 0, 0);
        List<String> curTip = new ArrayList<String>();
        r.inputTank.addTooltip(0, 0, curTip, false);
        r.outputTank.addTooltip(0, 0, curTip, false);

    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return GuiThermopneumaticProcessingPlant.class;
    }

    @Override
    public int recipiesPerPage(){
        return 1;
    }

    @Override
    public void loadTransferRects(){
        transferRects.add(new RecipeTransferRect(new Rectangle(25, 20, 48, 22), RECT_RECIPE));
    }

    @Override
    public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe){
        ThermoNEIRecipe r = (ThermoNEIRecipe)arecipes.get(recipe);
        transferRects.clear();
        loadTransferRects();
        transferRects.add(new RecipeTransferRect(r.inputTank.getBounds(), "liquid", r.inputTank.getFluid()));
        transferRects.add(new RecipeTransferRect(r.outputTank.getBounds(), "liquid", r.outputTank.getFluid()));
        return super.keyTyped(gui, keyChar, keyCode, recipe);
    }

    @Override
    public boolean mouseClicked(GuiRecipe gui, int button, int recipe){
        ThermoNEIRecipe r = (ThermoNEIRecipe)arecipes.get(recipe);
        transferRects.clear();
        loadTransferRects();
        transferRects.add(new RecipeTransferRect(r.inputTank.getBounds(), "liquid", r.inputTank.getFluid()));
        transferRects.add(new RecipeTransferRect(r.outputTank.getBounds(), "liquid", r.outputTank.getFluid()));
        return super.mouseClicked(gui, button, recipe);
    }

    @Override
    public List<String> handleTooltip(GuiRecipe guiRecipe, List<String> currenttip, int recipe){
        //  super.handleTooltip(guiRecipe, currenttip, recipe);
        ThermoNEIRecipe r = (ThermoNEIRecipe)arecipes.get(recipe);
        if(GuiContainerManager.shouldShowTooltip(guiRecipe)) {
            Point mouse = GuiDraw.getMousePosition();
            Point offset = guiRecipe.getRecipePosition(recipe);
            Point relMouse = new Point(mouse.x - (guiRecipe.width - 176) / 2 - offset.x, mouse.y - (guiRecipe.height - 166) / 2 - offset.y);

            if(r.inputTank.getBounds().contains(relMouse)) {
                r.inputTank.addTooltip(mouse.x, mouse.y, currenttip, false);
            }
            if(r.outputTank.getBounds().contains(relMouse)) {
                r.outputTank.addTooltip(mouse.x, mouse.y, currenttip, false);
            }
        }
        return currenttip;
    }

    /*   
       private boolean tankClick(GuiRecipe gui, int recipe, boolean usage){
           Point pos = getMousePosition();
           Point offset = gui.getRecipePosition(recipe);
           Point relMouse = new Point(pos.x - gui.guiLeft - offsetx, pos.y - gui.guiTop - offsety);
       }*/

    private class ThermoNEIRecipe extends CachedRecipe{
        public final BasicThermopneumaticProcessingPlantRecipe recipe;
        public final WidgetTank inputTank, outputTank;

        private ThermoNEIRecipe(BasicThermopneumaticProcessingPlantRecipe recipe){
            this.recipe = recipe;
            inputTank = new WidgetTank(8, 4, recipe.getInputLiquid());
            outputTank = new WidgetTank(74, 4, recipe.getOutputLiquid());
            int maxFluid = Math.max(inputTank.getTank().getFluidAmount(), outputTank.getTank().getFluidAmount());
            if(maxFluid <= 100) {
                inputTank.getTank().setCapacity(100);
                outputTank.getTank().setCapacity(100);
            } else if(maxFluid <= 1000) {
                inputTank.getTank().setCapacity(1000);
                outputTank.getTank().setCapacity(1000);
            }
        }

        @Override
        public PositionedStack getResult(){
            return null;
        }

        @Override
        public PositionedStack getIngredient(){
            return recipe.getInputItem() != null ? new PositionedStack(recipe.getInputItem(), 41, 3) : null;
        }

    }
}
