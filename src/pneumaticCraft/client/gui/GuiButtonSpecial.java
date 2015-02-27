package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.IWidgetListener;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * Extension of GuiButton that allows a invisible clickable field. It can be added in Gui's like buttons (with the buttonList).
 */

public class GuiButtonSpecial extends GuiButton implements IGuiWidget{

    private ItemStack[] renderedStacks;
    private List<String> tooltipText;
    private final RenderItem itemRenderer = new RenderItem();
    private int invisibleHoverColor;
    private boolean thisVisible = true;
    private IWidgetListener listener;

    public GuiButtonSpecial(int buttonID, int startX, int startY, int xSize, int ySize, String buttonText){
        super(buttonID, startX, startY, xSize, ySize, buttonText);
    }

    public void setVisible(boolean visible){
        thisVisible = visible;
    }

    public void setInvisibleHoverColor(int color){
        invisibleHoverColor = color;
    }

    public void setRenderStacks(ItemStack... renderedStacks){
        this.renderedStacks = renderedStacks;
    }

    public void setTooltipText(List<String> tooltip){
        tooltipText = tooltip;
    }

    public void setTooltipText(String tooltip){
        tooltipText = tooltip == null || tooltip.equals("") ? null : Arrays.asList(new String[]{tooltip});
    }

    public void getTooltip(List<String> curTooltip){
        if(tooltipText != null) {
            curTooltip.addAll(tooltipText);
        }
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y){
        if(thisVisible) super.drawButton(mc, x, y);

        if(visible) {
            if(renderedStacks != null) {
                int middleX = xPosition + width / 2;
                int startX = middleX - renderedStacks.length * 9 + 1;
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.enableGUIStandardItemLighting();
                for(int i = 0; i < renderedStacks.length; i++) {
                    itemRenderer.renderItemAndEffectIntoGUI(FMLClientHandler.instance().getClient().fontRenderer, FMLClientHandler.instance().getClient().renderEngine, renderedStacks[i], startX + i * 18, yPosition + 2);
                }
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            }
            if(enabled && !thisVisible && x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height) {
                Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, invisibleHoverColor);
            }
        }
    }

    @Override
    public void setListener(IWidgetListener gui){
        listener = gui;
    }

    @Override
    public int getID(){
        return id;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        if(mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            func_146113_a(Minecraft.getMinecraft().getSoundHandler());
            listener.actionPerformed(this);
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button){

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
        return false;
    }

    @Override
    public void update(){

    }

    @Override
    public void handleMouseInput(){}

}
