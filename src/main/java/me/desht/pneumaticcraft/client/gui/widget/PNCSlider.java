/*
 * Minecraft Forge - Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import javax.annotation.Nullable;
import java.text.DecimalFormat;


/**
 * Rewrite of Forge Slider class which can go away if/when
 * https://github.com/MinecraftForge/MinecraftForge/issues/8485 is resolved
 */
public class PNCSlider extends ExtendedButton {
    public double sliderValue;  // clamped to a 0..1 range
    public Component dispString;
    public boolean showDecimal;
    public double minValue;
    public double maxValue;
    public int precision;
    @Nullable
    public ISlider parent;
    public Component suffix;
    public boolean drawString;

    public PNCSlider(int xPos, int yPos, int width, int height, Component prefix, Component suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, OnPress handler) {
        this(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler, null);
    }

    public PNCSlider(int xPos, int yPos, int width, int height, Component prefix, Component suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, OnPress handler, @Nullable ISlider par) {
        super(xPos, yPos, width, height, prefix, handler);
        minValue = minVal;
        maxValue = maxVal;
        sliderValue = (currentVal - minValue) / (maxValue - minValue);
        dispString = prefix;
        parent = par;
        suffix = suf;
        showDecimal = showDec;
        String val;

        if (showDecimal) {
            val = Double.toString(sliderValue * (maxValue - minValue) + minValue);
            precision = Math.min(val.substring(val.indexOf(".") + 1).length(), 4);
        } else {
            val = Integer.toString((int) Math.round(sliderValue * (maxValue - minValue) + minValue));
            precision = 0;
        }

        setMessage(new TextComponent("").append(dispString).append(val).append(suffix));

        drawString = drawStr;
        if (!drawString)
            setMessage(new TextComponent(""));
    }

    public PNCSlider(int xPos, int yPos, Component displayStr, double minVal, double maxVal, double currentVal, OnPress handler, ISlider par) {
        this(xPos, yPos, 150, 20, displayStr, new TextComponent(""), minVal, maxVal, currentVal, true, true, handler, par);
    }

    @Override
    public int getYImage(boolean par1) {
        return 0;
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int par2, int par3) {
        if (this.visible) {
            GuiUtils.drawContinuousTexturedBox(poseStack, WIDGETS_LOCATION, this.x + (int) (this.sliderValue * (float) (this.width - 8)), this.y, 0, 66, 8, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.sliderValue = (mouseX - (this.x + 4)) / (this.width - 8);
        updateSlider();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.sliderValue = (mouseX - (this.x + 4)) / (this.width - 8);
        updateSlider();
    }

    public void updateSlider() {
        sliderValue = Mth.clamp(sliderValue, 0f, 1f);

        if (drawString) {
            String val;
            if (showDecimal && precision > 0) {
                val = new DecimalFormat("#." + Strings.repeat("#", precision))
                        .format(Mth.lerp(sliderValue, minValue, maxValue));
            } else {
                val = Integer.toString((int) Math.round(Mth.lerp(sliderValue, minValue, maxValue)));
            }
            setMessage(new TextComponent("").append(dispString).append(val).append(suffix));
        }

        if (parent != null) {
            parent.onChangeSliderValue(this);
        }
    }

    public int getValueInt() {
        return (int) Math.round(sliderValue * (maxValue - minValue) + minValue);
    }

    public double getValue() {
        return sliderValue * (maxValue - minValue) + minValue;
    }

    public void setValue(double d) {
        this.sliderValue = (d - minValue) / (maxValue - minValue);
    }

    @FunctionalInterface
    public interface ISlider {
        void onChangeSliderValue(PNCSlider slider);
    }
}
