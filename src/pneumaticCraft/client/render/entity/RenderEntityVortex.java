package pneumaticCraft.client.render.entity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.common.entity.projectile.EntityVortex;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityVortex extends RenderEntity{

    // ModelAirCannon model;
    private static final ResourceLocation texture = new ResourceLocation("pneumaticcraft:textures/items/" + Textures.ITEM_VORTEX + ".png");

    public RenderEntityVortex(){
        // model = new ModelAirCannon();
    }

    public void renderVortex(EntityVortex entity, double x, double y, double z, float var1, float partialTicks){

        int circlePoints = 200;
        double radius = 0.5D;
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(0.8, 0.8, 0.8D, 0.7D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);

        for(int i = 0; i < circlePoints; i++) {
            float angleRadians = (float)i / (float)circlePoints * 2F * (float)Math.PI;
            GL11.glPushMatrix();
            GL11.glTranslated(radius * Math.sin(angleRadians), radius * Math.cos(angleRadians), 0);
            renderGust();
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

    }

    /*
     * private void renderGust(Icon icon){ float f3 = icon.getMinU(); float f4 =
     * icon.getMaxU(); float f5 = icon.getMinV(); float f6 = icon.getMaxV();
     * float f7 = 1.0F; float f8 = 0.5F; float f9 = 0.25F; Tessellator
     * tessellator = Tessellator.instance; tessellator.startDrawingQuads();
     * tessellator.setNormal(0.0F, 1.0F, 0.0F);
     * tessellator.addVertexWithUV((double)(0.0F - f8), (double)(0.0F - f9),
     * 0.0D, (double)f3, (double)f6); tessellator.addVertexWithUV((double)(f7 -
     * f8), (double)(0.0F - f9), 0.0D, (double)f4, (double)f6);
     * tessellator.addVertexWithUV((double)(f7 - f8), (double)(1.0F - f9), 0.0D,
     * (double)f4, (double)f5); tessellator.addVertexWithUV((double)(0.0F - f8),
     * (double)(1.0F - f9), 0.0D, (double)f3, (double)f5); tessellator.draw(); }
     */

    private void renderGust(){
        byte b0 = 0;
        //float f2 = 0.0F;
        //float f3 = 0.5F;
        //float f4 = (0 + b0 * 10) / 16.0F;
        // float f5 = (5 + b0 * 10) / 16.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = (5 + b0 * 10) / 16.0F;
        float f9 = (10 + b0 * 10) / 16.0F;
        float f10 = 0.05625F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(f10, f10, f10);
        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
        GL11.glNormal3f(f10, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, f6, f8);
        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, f7, f8);
        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, f7, f9);
        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, f6, f9);

        double start = 0d;
        double end = 1 / 16d;
        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, start, start);
        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, start, end);
        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, end, end);
        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, end, start);
        tessellator.draw();
        GL11.glNormal3f(-f10, 0.0F, 0.0F);

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, f6, f8);
        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, f7, f8);
        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, f7, f9);
        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, f6, f9);
        tessellator.draw();

    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9){
        renderVortex((EntityVortex)par1Entity, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity){
        return texture;
    }
}
