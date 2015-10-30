package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import org.apache.commons.lang3.StringUtils;

public class WidgetTextField extends GuiTextField implements IGuiWidget{

    protected IWidgetListener listener;
    private final List<String> tooltip = new ArrayList<String>();
    private boolean passwordBox;

    public WidgetTextField(FontRenderer fontRenderer, int x, int y, int width, int height){
        super(fontRenderer, x, y, width, height);
    }

    @Override
    public void setListener(IWidgetListener gui){
        listener = gui;
    }

    public WidgetTextField setAsPasswordBox(){
        passwordBox = true;
        return this;
    }

    @Override
    public int getID(){
        return -1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        String oldText = getText();
        int oldCursorPos = getCursorPosition();
        if(passwordBox) {
            setText(StringUtils.repeat('*', oldText.length()));
            setCursorPosition(oldCursorPos);
        }
        drawTextBox();
        if(passwordBox) {
            setText(oldText);
            setCursorPosition(oldCursorPos);
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        mouseClicked(mouseX, mouseY, button);
        if(isFocused() && button == 1) {
            setText("");
            listener.onKeyTyped(this);
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button){
        onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public Rectangle getBounds(){
        return new Rectangle(xPosition, yPosition, width, height);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed){
        curTooltip.addAll(tooltip);
    }

    public void setTooltip(String... tooltip){
        this.tooltip.clear();
        for(String s : tooltip)
            this.tooltip.add(s);
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

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick){}

}
