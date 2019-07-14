package me.desht.pneumaticcraft.client.gui.widget;

import java.util.List;

public interface ITooltipSupplier {
    void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift);
}
