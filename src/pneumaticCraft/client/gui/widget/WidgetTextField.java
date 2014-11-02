package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class WidgetTextField extends GuiTextField implements IGuiWidget{

    private IWidgetListener listener;

    public WidgetTextField(FontRenderer fontRenderer, int x, int y, int width, int height){
        super(fontRenderer, x, y, width, height);
    }

    @Override
    public void setListener(IWidgetListener gui){
        listener = gui;
    }

    @Override
    public int getID(){
        return -1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        drawTextBox();
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public Rectangle getBounds(){
        return new Rectangle(xPosition, yPosition, width, height);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed){

    }

    @Override
    public boolean onKey(char key, int keyCode){
        if(textboxKeyTyped(key, keyCode)) {
            listener.onKeyTyped(this);
            return true;
        }
        return false;
    }

    @Override
    public void update(){

    }

    @Override
    public void handleMouseInput(){

    }

}
