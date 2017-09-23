package igwmod.gui;

public interface IClickable extends IReservedSpace, IWidget{
    boolean onMouseClick(GuiWiki gui, int x, int y);
}
