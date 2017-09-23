package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PneumaticCraftCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {
    private final IGuiHelper guiHelper;
    private final ResourceDrawable background = getGuiTexture();
    private static ITickTimer tickTimer;

    public PneumaticCraftCategory(IJeiHelpers jeiHelpers) {
        this.guiHelper = jeiHelpers.getGuiHelper();
        tickTimer = guiHelper.createTickTimer(60, 60, false);
    }

    @Nonnull
    @Override
    public String getModName() {
        return Names.MOD_ID;
    }

    public static class MultipleInputOutputRecipeWrapper implements IRecipeWrapper {
        private final List<PositionedStack> input = new ArrayList<>();
        private final List<PositionedStack> output = new ArrayList<>();
        private final List<WidgetTank> inputLiquids = new ArrayList<>();
        private final List<WidgetTank> outputLiquids = new ArrayList<>();
        private final List<IGuiWidget> tooltipWidgets = new ArrayList<>();
        private float pressure;
        private boolean usePressure;
        private int gaugeX, gaugeY;
        private WidgetTemperature tempWidget;
        private IHeatExchangerLogic heatExchanger;

        @Override
        public void getIngredients(@Nonnull IIngredients ingredients) {
            ingredients.setInputLists(ItemStack.class, input.stream().map(PositionedStack::getStacks).collect(Collectors.toList()));
            ingredients.setInputs(FluidStack.class, inputLiquids.stream().map(WidgetTank::getFluid).collect(Collectors.toList()));
            ingredients.setOutputLists(ItemStack.class, output.stream().map(PositionedStack::getStacks).collect(Collectors.toList()));
            ingredients.setOutputs(FluidStack.class, outputLiquids.stream().map(WidgetTank::getFluid).collect(Collectors.toList()));
        }

        void addIngredient(PositionedStack stack) {
            input.add(stack);
        }

        public void addIngredient(PositionedStack[] stacks) {
            Collections.addAll(input, stacks);
        }

        void addOutput(PositionedStack stack) {
            output.add(stack);
        }

        void addInputLiquid(FluidStack liquid, int x, int y) {
            WidgetTank tank = new WidgetTank(x, y, liquid);
            addInputLiquid(tank);
        }

        void addInputLiquid(WidgetTank tank) {
            inputLiquids.add(tank);
            recalculateTankSizes();
        }

        void addOutputLiquid(FluidStack liquid, int x, int y) {
            WidgetTank tank = new WidgetTank(x, y, liquid);
            addOutputLiquid(tank);
        }

        void addOutputLiquid(WidgetTank tank) {
            outputLiquids.add(tank);
            recalculateTankSizes();
        }

        private void recalculateTankSizes() {
            int maxFluid = 0;
            for (WidgetTank w : inputLiquids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }
            for (WidgetTank w : outputLiquids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }

            if (maxFluid <= 10) {
                maxFluid = 10;
            } else if (maxFluid <= 100) {
                maxFluid = 100;
            } else if (maxFluid <= 1000) {
                maxFluid = 1000;
            } else {
                maxFluid = 16000;
            }
            for (WidgetTank w : inputLiquids) {
                w.getTank().setCapacity(maxFluid);
            }
            for (WidgetTank w : outputLiquids) {
                w.getTank().setCapacity(maxFluid);
            }
        }

        protected void addWidget(IGuiWidget widget) {
            tooltipWidgets.add(widget);
        }

        void setUsedPressure(int x, int y, float pressure) {
            usePressure = true;
            this.pressure = pressure;
            gaugeX = x;
            gaugeY = y;
        }

        void setUsedTemperature(int x, int y, double temperature) {
            tempWidget = new WidgetTemperature(0, x, y, 273, 673,
                    heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic(), (int) temperature);
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            for (IGuiWidget widget : tooltipWidgets) {
                widget.render(0, 0, 0);
            }
            if (usePressure) {
                drawAnimatedPressureGauge(gaugeX, gaugeY, -1, pressure, 5, 7);
            }
            if (tempWidget != null) {
                heatExchanger.setTemperature(tickTimer.getValue() * (tempWidget.getScales()[0] - 273) / tickTimer.getMaxValue() + 273);
                tempWidget.render(0, 0, 0);
            }
        }

        @Nonnull
        @Override
        public List<String> getTooltipStrings(int mouseX, int mouseY) {
            List<String> currenttip = new ArrayList<String>();

            Point mouse = new Point(mouseX, mouseY);
            for (IGuiWidget widget : tooltipWidgets) {
                if (widget.getBounds().contains(mouse)) {
                    widget.addTooltip(mouse.x, mouse.y, currenttip, false);
                }
            }
            if (tempWidget != null) {
                if (tempWidget.getBounds().contains(mouse)) {
                    heatExchanger.setTemperature(tempWidget.getScales()[0]);
                    tempWidget.addTooltip(mouse.x, mouse.y, currenttip, false);
                }
            }

            return currenttip;
        }

        @Override
        public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
            return false;
        }

    }

    public static void drawAnimatedPressureGauge(int x, int y, float minPressure, float minWorkingPressure, float dangerPressure, float maxPressure) {
        GuiUtils.drawPressureGauge(FMLClientHandler.instance().getClient().fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, minWorkingPressure * ((float) tickTimer.getValue() / tickTimer.getMaxValue()), x, y, 90);
    }

    public abstract ResourceDrawable getGuiTexture();

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    void drawProgressBar(int x, int y, int u, int v, int width, int height, IDrawableAnimated.StartDirection startDirection) {
        IDrawableStatic drawable = guiHelper.createDrawable(background.getResource(), u, v, width, height);
        IDrawableAnimated animation = guiHelper.createAnimatedDrawable(drawable, 60, startDirection, false);
        animation.draw(Minecraft.getMinecraft(), x, y);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof PneumaticCraftCategory.MultipleInputOutputRecipeWrapper) {
            MultipleInputOutputRecipeWrapper recipe = (MultipleInputOutputRecipeWrapper) recipeWrapper;

            for (int i = 0; i < ingredients.getInputs(ItemStack.class).size(); i++) {
                recipeLayout.getItemStacks().init(i, true, recipe.input.get(i).getX() - 1, recipe.input.get(i).getY() - 1);
                recipeLayout.getItemStacks().set(i, recipe.input.get(i).getStacks());
            }

            for (int i = 0; i < ingredients.getOutputs(ItemStack.class).size(); i++) {
                recipeLayout.getItemStacks().init(i + recipe.input.size(), false, recipe.output.get(i).getX() - 1, recipe.output.get(i).getY() - 1);
                recipeLayout.getItemStacks().set(i + recipe.input.size(), recipe.output.get(i).getStacks());
            }

            IDrawable tankOverlay = new ResourceDrawable(Textures.WIDGET_TANK, 0, 0, 0, 0, 16, 64, 16, 64);
            for (int i = 0; i < ingredients.getInputs(FluidStack.class).size(); i++) {
                WidgetTank tank = recipe.inputLiquids.get(i);
                recipeLayout.getFluidStacks().init(i, true, tank.x, tank.y, tank.getBounds().width, tank.getBounds().height, tank.getTank().getCapacity(), true, tankOverlay);
                recipeLayout.getFluidStacks().set(i, tank.getFluid());
            }

            for (int i = 0; i < ingredients.getOutputs(FluidStack.class).size(); i++) {
                WidgetTank tank = recipe.outputLiquids.get(i);
                recipeLayout.getFluidStacks().init(recipe.inputLiquids.size() + i, false, tank.x, tank.y, tank.getBounds().width, tank.getBounds().height, tank.getTank().getCapacity(), true, tankOverlay);
                recipeLayout.getFluidStacks().set(recipe.inputLiquids.size() + i, tank.getFluid());
            }

        }
    }
}