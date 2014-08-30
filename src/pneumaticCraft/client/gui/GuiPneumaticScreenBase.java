package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.IWidgetListener;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class GuiPneumaticScreenBase extends GuiScreen implements IWidgetListener{
    /**
     * Any GuiAnimatedStat added to this list will be tracked for mouseclicks, tooltip renders, rendering,updating (resolution and expansion).
     */
    protected List<IGuiAnimatedStat> animatedStatList = new ArrayList<IGuiAnimatedStat>();
    protected final List<IGuiWidget> widgetList = new ArrayList<IGuiWidget>();
    public int guiLeft, guiTop, xSize, ySize;

    @Override
    public void initGui(){
        super.initGui();
        widgetList.clear();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    public void addWidget(IGuiWidget widget){
        widgetList.add(widget);
        widget.setListener(this);
    }

    protected abstract ResourceLocation getTexture();

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        if(getTexture() != null) {
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
        super.drawScreen(x, y, partialTicks);

        for(IGuiWidget checkBox : widgetList) {
            checkBox.render(x, y);
        }
        for(IGuiAnimatedStat stat : animatedStatList) {
            stat.render(fontRendererObj, zLevel, partialTicks);
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
        for(IGuiWidget widget : widgetList) {
            if(widget.getBounds().contains(x, y)) widget.addTooltip(tooltip, shift);
        }
        if(!tooltip.isEmpty()) {
            List<String> localizedTooltip = new ArrayList<String>();
            for(String line : tooltip) {
                String localizedLine = I18n.format(line);
                String[] lines = WordUtils.wrap(localizedLine, 50).split(System.getProperty("line.separator"));
                for(String locLine : lines) {
                    localizedTooltip.add(locLine);
                }
            }
            drawHoveringText(localizedTooltip, x, y, fontRendererObj);
        }
        for(IGuiAnimatedStat stat : animatedStatList) {
            stat.onMouseHovering(fontRendererObj, x, y);
            GL11.glDisable(GL11.GL_LIGHTING);
        }
    }

    /**
     * Copied from GuiContainer#drawHoveringText()
     * @param tooltip
     * @param x
     * @param y
     */
    private void drawTooltip(List<String> tooltip, int x, int y){
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        int k = 0;
        Iterator iterator = tooltip.iterator();

        while(iterator.hasNext()) {
            String s = (String)iterator.next();
            int l = fontRendererObj.getStringWidth(s);

            if(l > k) {
                k = l;
            }
        }

        int i1 = x + 12;
        int j1 = y - 12;
        int k1 = 8;

        if(tooltip.size() > 1) {
            k1 += 2 + (tooltip.size() - 1) * 10;
        }

        if(i1 + k > width) {
            i1 -= 28 + k;
        }

        if(j1 + k1 + 6 > height) {
            j1 = height - k1 - 6;
        }

        zLevel = 300.0F;
        int l1 = -267386864;
        drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
        drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
        drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
        drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
        drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
        int i2 = 1347420415;
        int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
        drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
        drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
        drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
        drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

        for(int k2 = 0; k2 < tooltip.size(); ++k2) {
            String s1 = tooltip.get(k2);
            fontRendererObj.drawStringWithShadow(s1, i1, j1, -1);

            if(k2 == 0) {
                j1 += 2;
            }

            j1 += 10;
        }

        zLevel = 0.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        for(IGuiAnimatedStat stat : animatedStatList) {
            if(stat.mouseClicked(par1, par2, par3)) {
                for(IGuiAnimatedStat closingStat : animatedStatList) {
                    if(stat != closingStat && stat.isLeftSided() == closingStat.isLeftSided()) {//when the stat is on the same side, close it.
                        closingStat.closeWindow();
                    }
                }
            }
        }
        for(IGuiWidget widget : widgetList) {
            if(widget.getBounds().contains(par1, par2)) widget.onMouseClicked(par1, par2, par3);
        }
    }

    protected void keyTyped(char key, int keyCode){
        if(keyCode == 1) {
            super.keyTyped(key, keyCode);
        }else{
            for(IGuiWidget widget : widgetList) {
                widget.onKey(key, keyCode);
            }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget){}

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3){
        animatedStatList.clear();
        widgetList.clear();
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
