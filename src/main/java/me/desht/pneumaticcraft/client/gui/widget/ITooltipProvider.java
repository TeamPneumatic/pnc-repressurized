package me.desht.pneumaticcraft.client.gui.widget;

import java.util.List;

public interface ITooltipProvider {
    void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift);
}
