package igwmod.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import igwmod.gui.tabs.EntityWikiTab;
import igwmod.lib.Util;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.client.FMLClientHandler;

public class LocatedEntity extends Gui implements IReservedSpace, IPageLink{
    protected static FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
    public final Entity entity;
    private int x, y;
    private final float scale;

    public LocatedEntity(Class<? extends Entity> clazz, int x, int y){
        this(clazz, x, y, 0.5F);
    }

    public LocatedEntity(Class<? extends Entity> clazz, int x, int y, float scale){
        entity = Util.getEntityForClass(clazz);
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        if(GuiWiki.MIN_TEXT_Y < y && GuiWiki.MAX_TEXT_Y > y + 16 * scale) {
            EntityWikiTab.drawEntity(entity, x + 16, y + 27, scale, 0);
        }
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){
        if(getReservedSpace().contains(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop())) {
            drawCreativeTabHoveringText(entity.getName(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
        }
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

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y){
        if(getReservedSpace().contains(x, y)) {
            gui.setCurrentFile(entity);
            return true;
        }
        return false;
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, 32, 32);
    }

    @Override
    public String getName(){
        return entity.getName();
    }

    @Override
    public String getLinkAddress(){
        return "entity/" + EntityList.getEntityString(entity);
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
        return 32;
    }
}
