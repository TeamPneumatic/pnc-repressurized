package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface ITooltipProvider {
    void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift);
}
