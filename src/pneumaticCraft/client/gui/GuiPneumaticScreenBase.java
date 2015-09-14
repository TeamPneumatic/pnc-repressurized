package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.IWidgetListener;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class GuiPneumaticScreenBase extends GuiScreen implements IWidgetListener{

    protected final List<IGuiWidget> widgets = new ArrayList<IGuiWidget>();
    public int guiLeft, guiTop, xSize, ySize;

    @Override
    public void initGui(){
        super.initGui();
        widgets.clear();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    public void addWidgets(Iterable<IGuiWidget> widgets){
        for(IGuiWidget widget : widgets) {
            addWidget(widget);
        }
    }

    public void addWidget(IGuiWidget widget){
        widgets.add(widget);
        widget.setListener(this);
    }

    protected void addLabel(String text, int x, int y){
        addWidget(new WidgetLabel(x, y, text));
    }

    public void removeWidget(IGuiWidget widget){
        widgets.remove(widget);
    }

    protected abstract ResourceLocation getTexture();

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        if(getTexture() != null) {
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
        super.drawScreen(x, y, partialTicks);

        for(IGuiWidget widget : widgets) {
            widget.render(x, y, partialTicks);
        }
        for(IGuiWidget widget : widgets) {
            widget.postRender(x, y, partialTicks);
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(1, 1, 1, 1);

        List<String> tooltip = new ArrayList<String>();
        for(Object obj : buttonList) {
            if(obj instanceof GuiButtonSpecial) {
                GuiButtonSpecial button = (GuiButtonSpecial)obj;
                if(button.xPosition < x && button.xPosition + button.getWidth() > x && button.yPosition < y && button.yPosition + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }
        boolean shift = PneumaticCraft.proxy.isSneakingInGui();
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(x, y)) widget.addTooltip(x, y, tooltip, shift);
        }
        if(!tooltip.isEmpty()) {
            List<String> localizedTooltip = new ArrayList<String>();
            for(String line : tooltip) {
                String localizedLine = I18n.format(line);
                for(String wrappedLine : localizedLine.split("\\\\n")) {
                    String[] lines = WordUtils.wrap(wrappedLine, 50).split(System.getProperty("line.separator"));
                    for(String locLine : lines) {
                        localizedTooltip.add(locLine);
                    }
                }
            }
            drawHoveringText(localizedTooltip, x, y, fontRendererObj);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(par1, par2)) widget.onMouseClicked(par1, par2, par3);
            else widget.onMouseClickedOutsideBounds(par1, par2, par3);
        }
    }

    @Override
    protected void keyTyped(char key, int keyCode){
        if(keyCode == 1) {
            super.keyTyped(key, keyCode);
        } else {
            for(IGuiWidget widget : widgets) {
                widget.onKey(key, keyCode);
            }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        if(widget instanceof IGuiAnimatedStat) {
            boolean leftSided = ((IGuiAnimatedStat)widget).isLeftSided();
            for(IGuiWidget w : widgets) {
                if(w instanceof IGuiAnimatedStat) {
                    IGuiAnimatedStat stat = (IGuiAnimatedStat)w;
                    if(widget != stat && stat.isLeftSided() == leftSided) {//when the stat is on the same side, close it.
                        stat.closeWindow();
                    }
                }
            }
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        for(IGuiWidget widget : widgets) {
            widget.update();
        }
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        for(IGuiWidget widget : widgets) {
            widget.handleMouseInput();
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){}

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3){
        widgets.clear();
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    public static void drawTexture(String texture, int x, int y){
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiUtils.getResourceLocation(texture));
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + 16, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + 16, y + 16, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + 16, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    public GuiButton getButtonFromRectangle(int buttonID, Rectangle buttonSize, String buttonText){
        return new GuiButton(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText);
    }

    public GuiButtonSpecial getInvisibleButtonFromRectangle(int buttonID, Rectangle buttonSize){
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "");
    }

    public GuiTextField getTextFieldFromRectangle(Rectangle textFieldSize){
        return new GuiTextField(fontRendererObj, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
    }

}
