package pneumaticCraft.common.thirdparty.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.gui.GuiUtils;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class PneumaticCraftPlugins extends TemplateRecipeHandler{
    public class MultipleInputOutputRecipe extends CachedRecipe{
        private final List<PositionedStack> input = new ArrayList<PositionedStack>();
        private final List<PositionedStack> output = new ArrayList<PositionedStack>();
        private final List<WidgetTank> inputLiquids = new ArrayList<WidgetTank>();
        private final List<WidgetTank> outputLiquids = new ArrayList<WidgetTank>();
        private final List<IGuiWidget> tooltipWidgets = new ArrayList<IGuiWidget>();
        private float pressure;
        private boolean usePressure;
        private int gaugeX, gaugeY;
        private WidgetTemperature tempWidget;
        private IHeatExchangerLogic heatExchanger;

        public void addIngredient(PositionedStack stack){
            input.add(stack);
        }

        public void addIngredient(PositionedStack[] stacks){
            for(PositionedStack stack : stacks) {
                input.add(stack);
            }
        }

        public void addOutput(PositionedStack stack){
            output.add(stack);
        }

        @Override
        public PositionedStack getResult(){
            return null;
        }

        @Override
        public List<PositionedStack> getIngredients(){
            return getCycledIngredients(cycleticks / 20, input);
        }

        @Override
        public List<PositionedStack> getOtherStacks(){
            return output;
        }

        protected void addInputLiquid(FluidStack liquid, int x, int y){
            WidgetTank tank = new WidgetTank(x, y, liquid);
            addInputLiquid(tank);
        }

        protected void addInputLiquid(WidgetTank tank){
            inputLiquids.add(tank);
            tooltipWidgets.add(tank);
            recalculateTankSizes();
        }

        protected void addOutputLiquid(FluidStack liquid, int x, int y){
            WidgetTank tank = new WidgetTank(x, y, liquid);
            addOutputLiquid(tank);
        }

        protected void addOutputLiquid(WidgetTank tank){
            outputLiquids.add(tank);
            tooltipWidgets.add(tank);
            recalculateTankSizes();
        }

        private void recalculateTankSizes(){
            int maxFluid = 0;
            for(WidgetTank w : inputLiquids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }
            for(WidgetTank w : outputLiquids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }

            if(maxFluid <= 10) {
                maxFluid = 10;
            } else if(maxFluid <= 100) {
                maxFluid = 100;
            } else if(maxFluid <= 1000) {
                maxFluid = 1000;
            } else {
                maxFluid = 16000;
            }
            for(WidgetTank w : inputLiquids) {
                w.getTank().setCapacity(maxFluid);
            }
            for(WidgetTank w : outputLiquids) {
                w.getTank().setCapacity(maxFluid);
            }
        }

        protected void addWidget(IGuiWidget widget){
            tooltipWidgets.add(widget);
        }

        protected void setUsedPressure(int x, int y, float pressure){
            usePressure = true;
            this.pressure = pressure;
            gaugeX = x;
            gaugeY = y;
        }

        protected void setUsedTemperature(int x, int y, double temperature){
            tempWidget = new WidgetTemperature(0, x, y, 273, 673, heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic(), (int)temperature);
        }

    }

    @Override
    public int recipiesPerPage(){
        return 1;
    }

    public void drawAnimatedPressureGauge(int x, int y, float minPressure, float minWorkingPressure, float dangerPressure, float maxPressure, float progress){
        GuiUtils.drawPressureGauge(FMLClientHandler.instance().getClient().fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, minWorkingPressure * progress, x, y, -90);
    }

    protected void addTransferRect(Rectangle rect){
        transferRects.add(new RecipeTransferRect(rect, getRecipeName()));
    }

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
        if(outputId.equals(getRecipeName())) loadAllRecipes();
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
        if(inputId.equals(getRecipeName())) loadAllRecipes();
        if(inputId.equals("liquid") && ingredients.length > 0 && ingredients[0] instanceof FluidStack) loadUsageRecipes((FluidStack)ingredients[0]);
    }

    @Override
    public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe){
        loadTankTransferRects(recipe);
        return super.keyTyped(gui, keyChar, keyCode, recipe);
    }

    @Override
    public boolean mouseClicked(GuiRecipe gui, int button, int recipe){
        loadTankTransferRects(recipe);
        return super.mouseClicked(gui, button, recipe);
    }

    private void loadTankTransferRects(int recipe){
        MultipleInputOutputRecipe r = (MultipleInputOutputRecipe)arecipes.get(recipe);
        transferRects.clear();
        loadTransferRects();
        for(WidgetTank tank : r.inputLiquids) {
            transferRects.add(new RecipeTransferRect(tank.getBounds(), "liquid", tank.getFluid()));
        }
        for(WidgetTank tank : r.outputLiquids) {
            transferRects.add(new RecipeTransferRect(tank.getBounds(), "liquid", tank.getFluid()));
        }
    }

    @Override
    public List<String> handleTooltip(GuiRecipe guiRecipe, List<String> currenttip, int recipe){
        //  super.handleTooltip(guiRecipe, currenttip, recipe);
        MultipleInputOutputRecipe r = (MultipleInputOutputRecipe)arecipes.get(recipe);
        if(GuiContainerManager.shouldShowTooltip(guiRecipe)) {
            Point mouse = GuiDraw.getMousePosition();
            Point offset = guiRecipe.getRecipePosition(recipe);
            Point relMouse = new Point(mouse.x - (guiRecipe.width - 176) / 2 - offset.x, mouse.y - (guiRecipe.height - 166) / 2 - offset.y);

            for(IGuiWidget widget : r.tooltipWidgets) {
                if(widget.getBounds().contains(relMouse)) {
                    widget.addTooltip(mouse.x, mouse.y, currenttip, false);
                }
            }
            if(r.tempWidget != null) {
                if(r.tempWidget.getBounds().contains(relMouse)) {
                    r.heatExchanger.setTemperature(r.tempWidget.getScales()[0]);
                    r.tempWidget.addTooltip(mouse.x, mouse.y, currenttip, false);
                }
            }
        }
        return currenttip;
    }

    @Override
    public void drawExtras(int recipe){
        MultipleInputOutputRecipe r = (MultipleInputOutputRecipe)arecipes.get(recipe);
        // drawAnimatedPressureGauge(120, 27, -1, r.getRequiredPressure(null, null), PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, cycleticks % 48 / 48F);
        for(IGuiWidget widget : r.tooltipWidgets) {
            widget.render(0, 0, 0);
        }
        if(r.usePressure) {
            drawAnimatedPressureGauge(r.gaugeX, r.gaugeY, -1, r.pressure, 5, 7, cycleticks % 48 / 48F);
        }
        if(r.tempWidget != null) {
            r.heatExchanger.setTemperature(cycleticks % 48 / 48F * (r.tempWidget.getScales()[0] - 273) + 273);
            r.tempWidget.render(0, 0, 0);
        }
    }

    protected abstract List<MultipleInputOutputRecipe> getAllRecipes();

    protected void loadCraftingRecipes(FluidStack stack){
        for(MultipleInputOutputRecipe recipe : getAllRecipes()) {
            for(WidgetTank tank : recipe.outputLiquids) {
                if(tank.getFluid() != null && tank.getFluid().getFluid() == stack.getFluid()) {
                    arecipes.add(recipe);
                }
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack output){
        for(MultipleInputOutputRecipe recipe : getAllRecipes()) {
            for(PositionedStack stack : recipe.output) {
                for(ItemStack itemStack : stack.items) {
                    if(NEIClientUtils.areStacksSameTypeCrafting(itemStack, output)) {
                        arecipes.add(recipe);
                    }
                }
            }
        }
    }

    protected void loadUsageRecipes(FluidStack stack){
        for(MultipleInputOutputRecipe recipe : getAllRecipes()) {
            for(WidgetTank tank : recipe.inputLiquids) {
                if(tank.getFluid() != null && tank.getFluid().getFluid() == stack.getFluid()) {
                    arecipes.add(recipe);
                }
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
        for(MultipleInputOutputRecipe recipe : getAllRecipes()) {
            for(PositionedStack stack : recipe.input) {
                for(ItemStack itemStack : stack.items) {
                    if(NEIClientUtils.areStacksSameTypeCrafting(itemStack, ingredient)) {
                        arecipes.add(recipe);
                    }
                }
            }
        }
    }

    protected void loadAllRecipes(){
        for(MultipleInputOutputRecipe recipe : getAllRecipes()) {
            arecipes.add(recipe);
        }
    }

}