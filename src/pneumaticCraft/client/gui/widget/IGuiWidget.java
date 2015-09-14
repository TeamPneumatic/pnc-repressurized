package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

public interface IGuiWidget{
    public void setListener(IWidgetListener gui);

    public int getID();

    public void render(int mouseX, int mouseY, float partialTick);

    public void postRender(int mouseX, int mouseY, float partialTick);

    public void onMouseClicked(int mouseX, int mouseY, int button);

    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button);

    public Rectangle getBounds();

    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed);

    public boolean onKey(char key, int keyCode);

    public void update();

    public void handleMouseInput();
}
