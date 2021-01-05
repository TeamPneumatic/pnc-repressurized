package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.common.tileentity.IRangedTE;

public class WidgetRangeToggleButton extends WidgetButtonExtended {
    public WidgetRangeToggleButton(int startX, int startY, IRangedTE te) {
        super(startX, startY, 16, 16, te.rangeText(), b -> {
            te.getRangeManager().toggleShowRange();
            b.setMessage(te.rangeText());
        });
    }
}
