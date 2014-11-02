package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

public class WidgetBase implements IGuiWidget{

    private final int id;
    public int value; //just a generic value
    protected final int x, y;
    private final int width;
    private final int height;
    protected IWidgetListener listener;

    public WidgetBase(int id, int x, int y, int width, int height){

        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getID(){

        return id;
    }

    @Override
    public void setListener(IWidgetListener gui){

        listener = gui;
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){

        listener.actionPerformed(this);
    }

    @Override
    public Rectangle getBounds(){

        return new Rectangle(x, y, width, height);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed){

    }

    @Override
    public boolean onKey(char key, int keyCode){
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){}

    @Override
    public void update(){}

    @Override
    public void handleMouseInput(){}

}
