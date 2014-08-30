package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

public interface IGuiWidget{
    public void setListener(IWidgetListener gui);

    public int getID();

    public void render(int mouseX, int mouseY);

    public void onMouseClicked(int mouseX, int mouseY, int button);

    public Rectangle getBounds();

    public void addTooltip(List<String> curTooltip, boolean shiftPressed);

    public void onKey(char key, int keyCode);
}
