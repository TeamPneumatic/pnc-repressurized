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

        setValidator(input -> {
            if (input == null || input.isEmpty() || input.equals("-")) {
                return true;  // treat as numeric zero
            }
            return NumberUtils.isCreatable(input);
        });
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

    public WidgetTextFieldNumber setValue(double value) {
        setText(PneumaticCraftUtils.roundNumberTo(value, decimals));
        return this;
    }

    public int getValue() {
        return MathHelper.clamp(NumberUtils.toInt(getText()), minValue, maxValue);
    }

    public double getDoubleValue() {
        return PneumaticCraftUtils.roundNumberToDouble(MathHelper.clamp(NumberUtils.toDouble(getText()), minValue, maxValue), decimals);
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
            setValue(MathHelper.clamp(getDoubleValue() + (delta > 0 ? adj : -adj), minValue, maxValue));
        } else {
            int v = getValue();
            if (v == 1 && adj % 10 == 0) adj--;  // little kludge to make adjusting from 1 behave in a user-friendly way
            setValue(MathHelper.clamp(getValue() + (delta > 0 ? adj : -adj), minValue, maxValue));
        }
        return true;
    }
}
