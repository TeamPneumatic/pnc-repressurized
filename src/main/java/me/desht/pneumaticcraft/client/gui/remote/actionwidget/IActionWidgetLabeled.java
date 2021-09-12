package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public interface IActionWidgetLabeled {
    void setText(ITextComponent text);

    ITextComponent getText();

    void setTooltip(List<ITextComponent> text);

    List<ITextComponent> getTooltip();

    default ITextComponent deserializeTextComponent(String s) {
        return s.startsWith("{") ? ITextComponent.Serializer.fromJson(s) : new StringTextComponent(s);
    }
}
