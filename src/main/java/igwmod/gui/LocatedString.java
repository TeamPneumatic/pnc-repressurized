package igwmod.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

public class LocatedString extends Gui implements IPageLink, GuiYesNoCallback{
    protected static FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
    private final String string;
    private String cappedText;
    private int x;
    private int y;
    private int color;
    private final boolean shadow;
    private String linkAddress;
    private GuiScreen parentGui;

    /**
     * A constructor for linked located strings. Color doesn't matter as these will always be the same for linked strings.
     * @param string
     * @param x
     * @param y
     * @param shadow
     * @param linkAddress
     */
    public LocatedString(String string, int x, int y, boolean shadow, String linkAddress){
        this.string = string;
        cappedText = string;
        this.x = x;
        this.y = y;
        this.shadow = shadow;
        this.linkAddress = linkAddress;
    }

    /**
     * A constructor for unlinked located strings. You can specify a color.
     * @param string
     * @param x
     * @param y
     * @param color
     * @param shadow
     */
    public LocatedString(String string, int x, int y, int color, boolean shadow){
        this.string = string;
        cappedText = string;
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
    }

    public LocatedString capTextWidth(int maxWidth){
        cappedText = string;
        if(fontRenderer.getStringWidth(cappedText) <= maxWidth) return this;
        while(fontRenderer.getStringWidth(cappedText + "...") > maxWidth) {
            cappedText = cappedText.substring(0, cappedText.length() - 1);
        }
        cappedText += "...";
        return this;
    }

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y){
        if(linkAddress != null) {
            if(getMouseSpace().contains(x, y)) {
                if(linkAddress.contains("www")) {
                    parentGui = Minecraft.getMinecraft().currentScreen;
                    Minecraft.getMinecraft().displayGuiScreen(new GuiConfirmOpenLink(this, linkAddress, 0, false));
                } else {
                    gui.setCurrentFile(linkAddress);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void confirmClicked(boolean result, int value){
        if(result) igwmod.lib.Util.openBrowser(linkAddress);
        Minecraft.getMinecraft().displayGuiScreen(parentGui);
    }

    private Rectangle getMouseSpace(){
        return new Rectangle((int)(x * GuiWiki.TEXT_SCALE), (int)(y * GuiWiki.TEXT_SCALE), (int)(fontRenderer.getStringWidth(cappedText) * GuiWiki.TEXT_SCALE), (int)(fontRenderer.FONT_HEIGHT * GuiWiki.TEXT_SCALE));
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        GL11.glPushMatrix();
        GL11.glScaled(GuiWiki.TEXT_SCALE, GuiWiki.TEXT_SCALE, 1);
        //GlStateManager.enableLighting();
        // RenderHelper.enableStandardItemLighting();
        if(getLinkAddress() != null) {
            Rectangle mouseSpace = getMouseSpace();
            fontRenderer.drawString(TextFormatting.UNDERLINE + cappedText, x, y, mouseSpace.contains(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop()) ? 0xFFFFFF00 : 0xFF3333FF, shadow);
        } else {
            GL11.glColor4d(0, 0, 0, 1);
            fontRenderer.drawString(cappedText, x, y, color, shadow);
        }
        GL11.glPopMatrix();
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){
        if(!cappedText.equals(string) && getMouseSpace().contains(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop())) {
            drawCreativeTabHoveringText(string, mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
        }
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, fontRenderer.getStringWidth(cappedText), fontRenderer.FONT_HEIGHT);
    }

    @Override
    public String getName(){
        return string;
    }

    @Override
    public String getLinkAddress(){
        return linkAddress;
    }

    @Override
    public String toString(){
        return x + ", " + y + ", string: " + string;
    }

    @Override
    public void setX(int x){
        this.x = x;
    }

    @Override
    public void setY(int y){
        this.y = y;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY(){
        return y;
    }

    protected void drawCreativeTabHoveringText(String par1Str, int par2, int par3){
        func_102021_a(Arrays.asList(par1Str), par2, par3);
    }

    protected void func_102021_a(List par1List, int par2, int par3){
        drawHoveringText(par1List, par2, par3, fontRenderer);
    }

    protected void drawHoveringText(List par1List, int par2, int par3, FontRenderer font){
        if(!par1List.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = par1List.iterator();

            while(iterator.hasNext()) {
                String s = (String)iterator.next();
                int l = font.getStringWidth(s);

                if(l > k) {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if(par1List.size() > 1) {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            /*  if(i1 + k > width) {
                  i1 -= 28 + k;
              }

              if(j1 + k1 + 6 > height) {
                  j1 = height - k1 - 6;
              }*/

            zLevel = 300.0F;
            //  itemRenderer.zLevel = 300.0F;
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

            for(int k2 = 0; k2 < par1List.size(); ++k2) {
                String s1 = (String)par1List.get(k2);
                font.drawStringWithShadow(s1, i1, j1, -1);

                if(k2 == 0) {
                    j1 += 2;
                }

                j1 += 10;
            }

            zLevel = 0.0F;
            //itemRenderer.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    @Override
    public int getHeight(){
        return fontRenderer.FONT_HEIGHT;
    }

}
