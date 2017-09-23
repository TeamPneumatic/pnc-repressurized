package igwmod.gui;

public interface IWidget{
    void renderBackground(GuiWiki gui, int mouseX, int mouseY);

    void renderForeground(GuiWiki gui, int mouseX, int mouseY);

    void setX(int x);

    void setY(int y);

    int getX();

    int getY();

    int getHeight();
}
