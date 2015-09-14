package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class WidgetBase implements IGuiWidget{

    private final int id;
    public int value; //just a generic value
    public int x, y;
    private final int width;
    private final int height;
    protected IWidgetListener listener;
    private final List<String> tooltipText = new ArrayList<String>();

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
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button){

    }

    @Override
    public Rectangle getBounds(){

        return new Rectangle(x, y, width, height);
    }

    public void setTooltipText(String tooltip){
        tooltipText.clear();
        if(tooltip != null && !tooltip.equals("")) {
            tooltipText.add(tooltip);
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed){
        curTip.addAll(tooltipText);
    }

    public String getTooltip(){
        return tooltipText.size() > 0 ? tooltipText.get(0) : "";
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

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick){

    }
}
