package igwmod.gui.tabs;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import igwmod.TickHandler;
import igwmod.api.WikiRegistry;
import igwmod.gui.GuiWiki;
import igwmod.gui.IPageLink;
import igwmod.gui.IReservedSpace;
import igwmod.gui.LocatedEntity;
import igwmod.gui.LocatedTexture;
import igwmod.lib.Textures;
import igwmod.lib.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EntityWikiTab implements IWikiTab{
    private static Entity curEntity;
    private static Entity tabEntity;

    public EntityWikiTab(){

    }

    @Override
    public String getName(){
        return "igwmod.wikitab.entities.name";
    }

    @Override
    public ItemStack renderTabIcon(GuiWiki gui){
        if(tabEntity == null) {
            EntityPlayer player = gui.mc.player;
            tabEntity = new EntityCreeper(player.world);
        }
        drawEntity(tabEntity, 18, 28, 0.6F, 0);
        return null;
    }

    @Override
    public List<IReservedSpace> getReservedSpaces(){
        List<IReservedSpace> reservedSpaces = new ArrayList<IReservedSpace>();
        reservedSpaces.add(new LocatedTexture(Textures.GUI_ENTITIES, 40, 74, 36, 153));
        return reservedSpaces;
    }

    @Override
    public List<IPageLink> getPages(int[] indexes){
        List<Class<? extends Entity>> allEntries = WikiRegistry.getEntityPageEntries();
        List<IPageLink> pages = new ArrayList<IPageLink>();
        if(indexes == null) {
            for(int i = 0; i < allEntries.size(); i++) {
                pages.add(new LocatedEntity(allEntries.get(i), 41, 77 + i * 36));
            }
        } else {
            for(int i = 0; i < indexes.length; i++) {
                pages.add(new LocatedEntity(allEntries.get(indexes[i]), 41, 77 + i * 36));
            }
        }
        return pages;
    }

    @Override
    public int pagesPerTab(){
        return 4;
    }

    @Override
    public int pagesPerScroll(){
        return 1;
    }

    @Override
    public int getSearchBarAndScrollStartY(){
        return 61;
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderHelper.enableStandardItemLighting();
        if(curEntity != null) drawEntity(curEntity, gui.getGuiLeft() + 65, gui.getGuiTop() + 49, 0.7F, 0);
        //  GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey){}

    /* public static void drawEntity(Entity entity, int x, int y, float size, float partialTicks){
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         short short1 = 240;
         short short2 = 240;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);

         GL11.glPushMatrix();
         GL11.glTranslated(x, y, 10);
         float maxHitboxComponent = Math.max(1, Math.max(entity.width, entity.height));
         GL11.glScaled(40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent, -40 * size / maxHitboxComponent);
         //GL11.glRotated(20, 1, 0, 1);
         GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
         GL11.glRotated(TickHandler.ticksExisted + partialTicks, 0, 1, 0);
         Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0D, 0D, 0.0D, 0, partialTicks);
         GL11.glPopMatrix();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_DEPTH_TEST);
     }*/

    public static void drawEntity(Entity entity, int x, int y, float size, float partialTicks){
        //GL11.glEnable(GL11.GL_LIGHTING);
        //x, y, scale, yaw, pitch
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float maxHitboxComponent = Math.max(1, Math.max(entity.width, entity.height));
        int scale = (int)(40 * size / maxHitboxComponent);

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 50.0F);
        GL11.glScalef(-scale, scale, scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        GL11.glRotatef(30, 1, 0, 0);

        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-TickHandler.ticksExisted, 0, 1, 0);
        /* GL11.glRotatef(-((float)Math.atan((double)(par4 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
         entity.renderYawOffset = (float)Math.atan((double)(par3 / 40.0F)) * 20.0F;
         entity.rotationYaw = (float)Math.atan((double)(par3 / 40.0F)) * 40.0F;
         entity.rotationPitch = -((float)Math.atan((double)(par4 / 40.0F))) * 20.0F;
         entity.rotationYawHead = entity.rotationYaw;
         entity.prevRotationYawHead = entity.rotationYaw;*/
        GL11.glTranslatef(0.0F, (float)entity.getYOffset(), 0.0F);
        Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        
        /* entity.renderYawOffset = f2;
         entity.rotationYaw = f3;
         entity.rotationPitch = f4;
         entity.prevRotationYawHead = f5;
         entity.rotationYawHead = f6;*/
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    public void onPageChange(GuiWiki gui, String pageName, Object... metadata){
        if(metadata.length > 0 && metadata[0] instanceof Entity) {
            curEntity = Util.getEntityForClass(((Entity)metadata[0]).getClass());
        }
    }

}
