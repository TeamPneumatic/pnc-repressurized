package me.desht.pneumaticcraft.client.gui.widget;

import java.awt.*;
import java.util.List;

public interface IGuiWidget {
    void setListener(IWidgetListener gui);

    int getID();

    void render(int mouseX, int mouseY, float partialTick);

    void postRender(int mouseX, int mouseY, float partialTick);

    void onMouseClicked(int mouseX, int mouseY, int button);

    void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button);

    Rectangle getBounds();

    void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed);

    boolean onKey(char key, int keyCode);

    void update();

    void handleMouseInput();
}
