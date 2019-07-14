package me.desht.pneumaticcraft.client.gui.widget;

import java.awt.*;
import java.util.List;

public interface IGuiWidget {
    void setListener(IWidgetListener gui);

    int getID();

    void render(int mouseX, int mouseY, float partialTick);

    void postRender(int mouseX, int mouseY, float partialTick);

    boolean onMouseClicked(double mouseX, double mouseY, int button);

    boolean onMouseClickedOutsideBounds(double mouseX, double mouseY, int button);

    Rectangle getBounds();

    void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed);

    boolean onKey(int keyCode, int modifiers);

    void update();

    void handleMouseInput();
}
