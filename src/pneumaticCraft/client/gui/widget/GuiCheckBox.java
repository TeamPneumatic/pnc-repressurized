package pneumaticCraft.client.gui.widget;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCheckBox extends Gui implements IGuiWidget{
    public boolean checked, enabled = true;
    public int x, y, color;
    private final int id;
    public String text;
    public FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
    private List<String> tooltip = new ArrayList<String>();
    private IWidgetListener listener;

    private static final int CHECKBOX_WIDTH = 10;
    private static final int CHECKBOX_HEIGHT = 10;

    public GuiCheckBox(int id, int x, int y, int color, String text){
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
        this.text = text;
    }

    @Override
    public int getID(){
        return id;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        drawRect(x, y, x + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT, enabled ? -6250336 : 0xFF999999);
        drawRect(x + 1, y + 1, x + CHECKBOX_WIDTH - 1, y + CHECKBOX_HEIGHT - 1, enabled ? -16777216 : 0xFFAAAAAA);
        if(checked) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if(enabled) {
                GL11.glColor4d(1, 1, 1, 1);
            } else {
                GL11.glColor4d(0.8, 0.8, 0.8, 1);
            }
            Tessellator t = Tessellator.instance;

            t.startDrawing(GL11.GL_LINE_STRIP);
            t.addVertex(x + 2, y + 5, zLevel);
            t.addVertex(x + 5, y + 7, zLevel);
            t.addVertex(x + 8, y + 3, zLevel);
            t.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        fontRenderer.drawString(I18n.format(text), x + 1 + CHECKBOX_WIDTH, y + CHECKBOX_HEIGHT / 2 - fontRenderer.FONT_HEIGHT / 2, enabled ? color : 0xFF888888);
    }

    @Override
    public Rectangle getBounds(){
        return new Rectangle(x, y, CHECKBOX_WIDTH + fontRenderer.getStringWidth(I18n.format(text)), CHECKBOX_HEIGHT);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        if(enabled) {
            checked = !checked;
            listener.actionPerformed(this);
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button){

    }

    public GuiCheckBox setTooltip(String tooltip){
        this.tooltip.clear();
        if(tooltip != null && !tooltip.equals("")) {
            this.tooltip.add(tooltip);
        }
        return this;
    }

    public GuiCheckBox setTooltip(List<String> tooltip){
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed){
        curTooltip.addAll(tooltip);
    }

    public String getTooltip(){
        return tooltip.size() > 0 ? tooltip.get(0) : "";
    }

    @Override
    public boolean onKey(char key, int keyCode){
        return false;
    }

    @Override
    public void setListener(IWidgetListener gui){
        listener = gui;
    }

    public GuiCheckBox setChecked(boolean checked){
        this.checked = checked;
        return this;
    }

    @Override
    public void update(){}

    @Override
    public void handleMouseInput(){}

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick){

    }
}
