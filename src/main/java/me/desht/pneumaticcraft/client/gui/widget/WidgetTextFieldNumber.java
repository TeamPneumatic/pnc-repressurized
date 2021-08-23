package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;

public class WidgetTextFieldNumber extends WidgetTextField {
    public int minValue = Integer.MIN_VALUE;
    public int maxValue = Integer.MAX_VALUE;
    private int decimals;
    private double fineAdjust = 1;
    private double coarseAdjust = 1000;

    public WidgetTextFieldNumber(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height);
        setValue(0);

        setFilter(input -> decimals == 0 ?
                PneumaticCraftUtils.isInteger(input) :
                PneumaticCraftUtils.isNumber(input)
        );
    }

    public WidgetTextFieldNumber setRange(int min, int max) {
        minValue = min;
        maxValue = max;
        return this;
    }

    public WidgetTextFieldNumber setDecimals(int decimals) {
        this.decimals = decimals;
        return this;
    }

    public WidgetTextFieldNumber setValue(int value) {
        setValue(Integer.toString(MathHelper.clamp(value, minValue, maxValue)));
        return this;
    }

    public WidgetTextFieldNumber setValue(double value) {
        setValue(PneumaticCraftUtils.roundNumberTo(MathHelper.clamp(value, minValue, maxValue), decimals));
        return this;
    }

    public int getIntValue() {
        return MathHelper.clamp(NumberUtils.toInt(getValue()), minValue, maxValue);
    }

    public double getDoubleValue() {
        return PneumaticCraftUtils.roundNumberToDouble(MathHelper.clamp(NumberUtils.toDouble(getValue()), minValue, maxValue), decimals);
    }

    public WidgetTextFieldNumber setAdjustments(double fineAdjust, double coarseAdjust) {
        this.fineAdjust = fineAdjust;
        this.coarseAdjust = coarseAdjust;
        return this;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double adj = ClientUtils.hasShiftDown() ? coarseAdjust : fineAdjust;
        if (decimals > 0) {
            setValue(getDoubleValue() + (delta > 0 ? adj : -adj));
        } else {
            int curVal = getIntValue();
            if (curVal == 1 && adj % 10 == 0) adj--;  // little kludge to make adjusting from 1 behave in a user-friendly way
            setValue(curVal + (delta > 0 ? adj : -adj));
        }
        return true;
    }

}
