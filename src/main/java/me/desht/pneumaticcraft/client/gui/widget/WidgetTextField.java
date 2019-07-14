package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetTextField extends TextFieldWidget {

    private final List<String> tooltip = new ArrayList<>();
    private boolean passwordBox;

    public WidgetTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height, "");
    }

    public WidgetTextField setAsPasswordBox() {
        passwordBox = true;
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        String oldText = getText();
        int oldCursorPos = getCursorPosition();
        if (passwordBox) {
            setText(StringUtils.repeat('*', oldText.length()));
            setCursorPosition(oldCursorPos);
        }
        super.render(mouseX, mouseY, partialTick);
        if (passwordBox) {
            setText(oldText);
            setCursorPosition(oldCursorPos);
        }
    }

    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        curTooltip.addAll(tooltip);
    }

    public void setTooltip(String... tooltip) {
        this.tooltip.clear();
        Collections.addAll(this.tooltip, tooltip);
    }

}
