package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.recipe.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipSupplier;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class JEIPneumaticCraftCategory<T> implements IRecipeCategory<T> {
    private final IGuiHelper guiHelper;
    private final ITickTimer tickTimer;

    private final List<Widget> subWidgets = new ArrayList<>();
    private float pressure;
    private float maxPressure;
    private float dangerPressure;
    private boolean drawPressureGauge;
    private int gaugeX, gaugeY;
    private WidgetTemperature tempWidget;
    private IHeatExchangerLogic heatExchanger;

    JEIPneumaticCraftCategory(IJeiHelpers helpers) {
        guiHelper = helpers.getGuiHelper();
        tickTimer = guiHelper.createTickTimer(60, 60, false);
    }

    void setUsedPressure(int x, int y, float pressure, float maxPressure, float dangerPressure) {
        this.drawPressureGauge = true;
        this.pressure = pressure;
        this.maxPressure = maxPressure;
        this.dangerPressure = dangerPressure;
        this.gaugeX = x;
        this.gaugeY = y;
    }

    void setUsedTemperature(int x, int y, TemperatureRange temp) {
        int[] scales;
        if (temp.getMax() < Integer.MAX_VALUE) {
            scales = new int[] { temp.getMin(), temp.getMax() };
        } else {
            scales = new int[] { temp.getMin() };
        }
        int max = temp.getMax() == Integer.MAX_VALUE ? 673 : Math.min(2000, temp.getMax() + 10);
        tempWidget = new WidgetTemperature(x, y, Math.max(0, temp.getMin() - 100), max,
                heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic(), scales) {
            @Override
            public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
                curTip.add("Required Temperature: " + (logic.getTemperatureAsInt() - 273) + "\u00b0C");
            }
        };
    }

    @Override
    public void draw(T recipe, double mouseX, double mouseY) {
        subWidgets.forEach(widget -> widget.render((int) mouseX, (int) mouseY, 0));

        if (tempWidget != null) {
            heatExchanger.setTemperature(tickTimer.getValue() * (tempWidget.getScales()[0] - 273.0) / tickTimer.getMaxValue() + 273.0);
            tempWidget.render(0, 0, 0);
        }

        if (drawPressureGauge) {
            drawAnimatedPressureGauge(gaugeX, gaugeY, pressure, dangerPressure, maxPressure);
        }
    }

    private void drawAnimatedPressureGauge(int x, int y, float minWorkingPressure, float dangerPressure, float maxPressure) {
        float p2 = minWorkingPressure * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
        GuiUtils.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, maxPressure, dangerPressure, minWorkingPressure, p2, x, y, 90);
    }

    @Override
    public List<String> getTooltipStrings(T recipe, double mouseX, double mouseY) {
        List<String> currenttip = new ArrayList<>();

        for (Widget widget : subWidgets) {
            if (widget instanceof ITooltipSupplier && widget.isMouseOver(mouseX, mouseY)) {
                ((ITooltipSupplier) widget).addTooltip((int)mouseX, (int)mouseY, currenttip, false);
            }
        }

        if (tempWidget != null && tempWidget.isMouseOver(mouseX, mouseY)) {
            heatExchanger.setTemperature(tempWidget.getScales()[0]);
            tempWidget.addTooltip((int)mouseX, (int)mouseY, currenttip, false);
        }

        if (drawPressureGauge
                && mouseX >= gaugeX - GuiUtils.PRESSURE_GAUGE_RADIUS && mouseX <= gaugeX + GuiUtils.PRESSURE_GAUGE_RADIUS
                && mouseY >= gaugeY - GuiUtils.PRESSURE_GAUGE_RADIUS && mouseY <= gaugeY + GuiUtils.PRESSURE_GAUGE_RADIUS) {
            currenttip.add(this.pressure + " bar");
        }

        return currenttip;
    }

    void addWidget(Widget widget) {
        subWidgets.add(widget);
    }

    void drawTextAt(String translationKey, int x, int y) {
        List<String> text = PneumaticCraftUtils.convertStringIntoList(I18n.format(translationKey), 30);
        int h = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
        for (int i = 0; i < text.size(); i++) {
            Minecraft.getInstance().fontRenderer.drawString(text.get(i), x, y + i * h, 0xFF404040);
        }
    }

    void drawIconAt(IDrawable icon, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableDepthTest();
        GlStateManager.enableAlphaTest();
        icon.draw(x, y);
        GlStateManager.enableDepthTest();
        GlStateManager.disableAlphaTest();
    }

    void drawProgressBar(ResourceLocation rl, int x, int y, int u, int v, int width, int height, IDrawableAnimated.StartDirection startDirection) {
        IDrawableStatic drawable = guiHelper.createDrawable(rl, u, v, width, height);
        IDrawableAnimated animation = guiHelper.createAnimatedDrawable(drawable, 60, startDirection, false);
        animation.draw(x, y);
    }
}
