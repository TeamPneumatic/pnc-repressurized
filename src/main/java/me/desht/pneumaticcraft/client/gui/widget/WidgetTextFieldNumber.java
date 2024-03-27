/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.NumberFormat;
import java.text.ParseException;

public class WidgetTextFieldNumber extends WidgetTextField {
    public int minValue = Integer.MIN_VALUE;
    public int maxValue = Integer.MAX_VALUE;
    private int decimals;
    private double fineAdjust = 1;
    private double coarseAdjust = 1000;

    public WidgetTextFieldNumber(Font fontRenderer, int x, int y, int width, int height) {
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
        setValue(Integer.toString(Mth.clamp(value, minValue, maxValue)));
        return this;
    }

    public WidgetTextFieldNumber setValue(double value) {
        setValue(PneumaticCraftUtils.roundNumberTo(Mth.clamp(value, minValue, maxValue), decimals));
        return this;
    }

    public int getIntValue() {
        return Mth.clamp(NumberUtils.toInt(getValue()), minValue, maxValue);
    }

    public double getDoubleValue() {
        try {
            // using NumberFormat here rather than NumberUtils; NumberFormat honours locale settings, which is
            // important, since locale settings are also honoured when putting a value into the widget
            Number n = NumberFormat.getNumberInstance().parse(getValue());
            return PneumaticCraftUtils.roundNumberToDouble(Mth.clamp(n.doubleValue(), minValue, maxValue), decimals);
        } catch (ParseException e) {
            return 0d;
        }
    }

    public WidgetTextFieldNumber setAdjustments(double fineAdjust, double coarseAdjust) {
        this.fineAdjust = fineAdjust;
        this.coarseAdjust = coarseAdjust;
        return this;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double adj = ClientUtils.hasShiftDown() ? coarseAdjust : fineAdjust;
        if (decimals > 0) {
            setValue(getDoubleValue() + (deltaY > 0 ? adj : -adj));
        } else {
            int curVal = getIntValue();
            if (curVal == 1 && adj % 10 == 0) adj--;  // little kludge to make adjusting from 1 behave in a user-friendly way
            setValue(curVal + (deltaY > 0 ? adj : -adj));
        }
        return true;
    }

}
