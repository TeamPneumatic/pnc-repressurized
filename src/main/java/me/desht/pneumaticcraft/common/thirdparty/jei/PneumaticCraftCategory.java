package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.lib.GuiConstants.DEGREES;

public abstract class PneumaticCraftCategory<T extends PneumaticCraftCategory.AbstractCategoryExtension> implements IRecipeCategory<T> {
    private static ITickTimer tickTimer;
    private final IGuiHelper guiHelper;

    PneumaticCraftCategory(IJeiHelpers helpers) {
        guiHelper = helpers.getGuiHelper();
        tickTimer = guiHelper.createTickTimer(60, 60, false);
    }

    @Override
    public void setIngredients(T recipeWrapper, IIngredients iIngredients) {
        recipeWrapper.setIngredients(iIngredients);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, T recipeWrapper, IIngredients ingredients) {
        List<PositionedStack> inputItems = recipeWrapper.inputItems;
        List<PositionedStack> outputItems = recipeWrapper.outputItems;
        List<WidgetTank> inputFluids = recipeWrapper.inputFluids;
        List<WidgetTank> outputFluids = recipeWrapper.outputFluids;

        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String key = slotIndex < inputItems.size() ?
                    inputItems.get(slotIndex).getTooltipKey() :
                    outputItems.get(slotIndex - inputItems.size()).getTooltipKey();
            if (key != null) {
                tooltip.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(key)));
            }
        });

        for (int i = 0; i < ingredients.getInputs(VanillaTypes.ITEM).size(); i++) {
            recipeLayout.getItemStacks().init(i, true, inputItems.get(i).getX() - 1, inputItems.get(i).getY() - 1);
            recipeLayout.getItemStacks().set(i, inputItems.get(i).getStacks());
        }

        for (int i = 0; i < ingredients.getOutputs(VanillaTypes.ITEM).size(); i++) {
            recipeLayout.getItemStacks().init(i + inputItems.size(), false, outputItems.get(i).getX() - 1, outputItems.get(i).getY() - 1);
            recipeLayout.getItemStacks().set(i + inputItems.size(), outputItems.get(i).getStacks());
        }

        for (int i = 0; i < ingredients.getInputs(VanillaTypes.FLUID).size(); i++) {
            WidgetTank tank = inputFluids.get(i);
            IDrawable tankOverlay = guiHelper.createDrawable(Textures.WIDGET_TANK, 0, 0, tank.getWidth(), tank.getHeight());
            recipeLayout.getFluidStacks().init(i, true, tank.x, tank.y, tank.getWidth(), tank.getHeight(), tank.getTank().getCapacity(), false, tankOverlay);
            recipeLayout.getFluidStacks().set(i, tank.getFluid());
        }

        for (int i = 0; i < ingredients.getOutputs(VanillaTypes.FLUID).size(); i++) {
            WidgetTank tank = outputFluids.get(i);
            IDrawable tankOverlay = guiHelper.createDrawable(Textures.WIDGET_TANK, 0, 0, tank.getWidth(), tank.getHeight());
            recipeLayout.getFluidStacks().init(inputFluids.size() + i, false, tank.x, tank.y, tank.getWidth(), tank.getHeight(), tank.getTank().getCapacity(), false, tankOverlay);
            recipeLayout.getFluidStacks().set(inputFluids.size() + i, tank.getFluid());
        }
    }

    static void drawIconAt(IDrawable icon, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableDepthTest();
        GlStateManager.enableAlphaTest();
        icon.draw(x, y);
        GlStateManager.enableDepthTest();
        GlStateManager.disableAlphaTest();
    }

    static void drawTextAt(String translationKey, int x, int y) {
        List<String> text = PneumaticCraftUtils.convertStringIntoList(I18n.format(translationKey), 30);
        int h = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
        for (int i = 0; i < text.size(); i++) {
            Minecraft.getInstance().fontRenderer.drawString(text.get(i), x, y + i * h, 0xFF404040);
        }
    }

    void drawProgressBar(ResourceLocation rl, int x, int y, int u, int v, int width, int height, IDrawableAnimated.StartDirection startDirection) {
        IDrawableStatic drawable = guiHelper.createDrawable(rl, u, v, width, height);
        IDrawableAnimated animation = guiHelper.createAnimatedDrawable(drawable, 60, startDirection, false);
        animation.draw(x, y);
    }

    public static abstract class AbstractCategoryExtension implements IRecipeCategoryExtension {
        final List<PositionedStack> inputItems = new ArrayList<>();
        final List<PositionedStack> outputItems = new ArrayList<>();
        final List<WidgetTank> inputFluids = new ArrayList<>();
        final List<WidgetTank> outputFluids = new ArrayList<>();
        private final List<Widget> subWidgets = new ArrayList<>();
        private float pressure;
        private float maxPressure;
        private float dangerPressure;
        private boolean drawPressureGauge;
        private int gaugeX, gaugeY;
        private WidgetTemperature tempWidget;
        private IHeatExchangerLogic heatExchanger;

        @Override
        public void setIngredients(@Nonnull IIngredients ingredients) {
            ingredients.setInputLists(VanillaTypes.ITEM, inputItems.stream().map(PositionedStack::getStacks).collect(Collectors.toList()));
            ingredients.setInputs(VanillaTypes.FLUID, inputFluids.stream().map(WidgetTank::getFluid).collect(Collectors.toList()));
            ingredients.setOutputLists(VanillaTypes.ITEM, outputItems.stream().map(PositionedStack::getStacks).collect(Collectors.toList()));
            ingredients.setOutputs(VanillaTypes.FLUID, outputFluids.stream().map(WidgetTank::getFluid).collect(Collectors.toList()));
        }

        void addInputItem(PositionedStack stack) {
            inputItems.add(stack);
        }

        public void addInputItem(PositionedStack[] stacks) {
            Collections.addAll(inputItems, stacks);
        }

        void addOutputItem(PositionedStack stack) {
            outputItems.add(stack);
        }

        void addInputFluid(FluidStack fluid, int x, int y) {
            addInputFluid(new WidgetTank(x, y, fluid));
        }

        void addInputFluid(WidgetTank tank) {
            inputFluids.add(tank);
            recalculateTankSizes();
        }

        void addOutputFluid(FluidStack fluid, int x, int y) {
            addOutputFluid(new WidgetTank(x, y, fluid));
        }

        void addOutputFluid(WidgetTank tank) {
            outputFluids.add(tank);
            recalculateTankSizes();
        }

        private void recalculateTankSizes() {
            int maxFluid = 0;
            for (WidgetTank w : inputFluids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }
            for (WidgetTank w : outputFluids) {
                maxFluid = Math.max(maxFluid, w.getTank().getFluidAmount());
            }
            for (WidgetTank w : inputFluids) {
                w.getTank().setCapacity(maxFluid);
            }
            for (WidgetTank w : outputFluids) {
                w.getTank().setCapacity(maxFluid);
            }
        }

        protected void addWidget(Widget widget) {
            subWidgets.add(widget);
        }

        void setUsedPressure(int x, int y, float pressure, float maxPressure, float dangerPressure) {
            this.drawPressureGauge = true;
            this.pressure = pressure;
            this.maxPressure = maxPressure;
            this.dangerPressure = dangerPressure;
            this.gaugeX = x;
            this.gaugeY = y;
        }

        void setUsedTemperature(int x, int y, double temperature) {
            tempWidget = new WidgetTemperature(x, y, 273, 673,
                    heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic(), (int) temperature) {
                @Override
                public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
                    curTip.add("Required Temperature: " + (logic.getTemperatureAsInt() - 273) + DEGREES + "C");
                }
            };
        }

        void drawAnimatedPressureGauge(int x, int y, float minPressure, float minWorkingPressure, float dangerPressure, float maxPressure) {
            float p2 = minWorkingPressure * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
            GuiUtils.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, maxPressure, dangerPressure, minWorkingPressure, p2, x, y);
        }

        @Override
        public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
            for (Widget widget : subWidgets) {
                widget.render(0, 0, 0);
            }
            if (drawPressureGauge) {
                drawAnimatedPressureGauge(gaugeX, gaugeY, -1,  pressure,5, 7);
            }
            if (tempWidget != null) {
                heatExchanger.setTemperature(tickTimer.getValue() * (tempWidget.getScales()[0] - 273.0) / tickTimer.getMaxValue() + 273.0);
                tempWidget.render(0, 0, 0);
            }
        }

        @Override
        public List<String> getTooltipStrings(double mouseX, double mouseY) {
            List<String> currenttip = new ArrayList<>();

            for (Widget widget : subWidgets) {
                if (widget instanceof ITooltipProvider && widget.isMouseOver(mouseX, mouseY)) {
                    ((ITooltipProvider)widget).addTooltip(mouseX, mouseY, currenttip, false);
                }
            }
            if (tempWidget != null && tempWidget.isMouseOver(mouseX, mouseY)) {
                heatExchanger.setTemperature(tempWidget.getScales()[0]);
                tempWidget.addTooltip(mouseX, mouseY, currenttip, false);
            }

            if (drawPressureGauge
                    && mouseX >= gaugeX - GuiUtils.PRESSURE_GAUGE_RADIUS && mouseX <= gaugeX + GuiUtils.PRESSURE_GAUGE_RADIUS
                    && mouseY >= gaugeY - GuiUtils.PRESSURE_GAUGE_RADIUS && mouseY <= gaugeY + GuiUtils.PRESSURE_GAUGE_RADIUS) {
                currenttip.add(this.pressure + " bar");
            }

            return currenttip;
        }
    }
}
