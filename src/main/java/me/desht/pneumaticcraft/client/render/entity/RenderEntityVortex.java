package me.desht.pneumaticcraft.client.render.entity;

import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderEntityVortex extends RenderEntity {

    private static final ResourceLocation TEXTURE = new ResourceLocation("pneumaticcraft:textures/items/" + Textures.ITEM_VORTEX + ".png");

    public static final IRenderFactory<EntityVortex> FACTORY = RenderEntityVortex::new;

    public RenderEntityVortex(RenderManager manager) {
        super(manager);
    }

    private void renderVortex(EntityVortex entity, double x, double y, double z, float var1, float partialTicks) {
        int circlePoints = 200;
        double radius = 0.5D;
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(0.8, 0.8, 0.8D, 0.7D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);

        for (int i = 0; i < circlePoints; i++) {
            float angleRadians = (float) i / (float) circlePoints * 2F * (float) Math.PI;
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
     * tessellator = Tessellator.instance; wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
     * tessellator.setNormal(0.0F, 1.0F, 0.0F);
     * wr.pos((double)(0.0F - f8), (double)(0.0F - f9),
     * 0.0D, (double)f3, (double)f6); wr.pos((double)(f7 -
     * f8), (double)(0.0F - f9), 0.0D, (double)f4, (double)f6);
     * wr.pos((double)(f7 - f8), (double)(1.0F - f9), 0.0D,
     * (double)f4, (double)f5); wr.pos((double)(0.0F - f8),
     * (double)(1.0F - f9), 0.0D, (double)f3, (double)f5); Tessellator.getInstance().draw(); }
     */

    private void renderGust() {
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
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        wr.pos(-7.0D, -2.0D, -2.0D).tex(f6, f8).endVertex();
        wr.pos(-7.0D, -2.0D, 2.0D).tex(f7, f8).endVertex();
        wr.pos(-7.0D, 2.0D, 2.0D).tex(f7, f9).endVertex();
        wr.pos(-7.0D, 2.0D, -2.0D).tex(f6, f9).endVertex();

        double start = 0d;
        double end = 1 / 16d;
        wr.pos(-7.0D, -2.0D, -2.0D).tex(start, start).endVertex();
        wr.pos(-7.0D, -2.0D, 2.0D).tex(start, end).endVertex();
        wr.pos(-7.0D, 2.0D, 2.0D).tex(end, end).endVertex();
        wr.pos(-7.0D, 2.0D, -2.0D).tex(end, start).endVertex();
        Tessellator.getInstance().draw();
        GL11.glNormal3f(-f10, 0.0F, 0.0F);

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-7.0D, 2.0D, -2.0D).tex(f6, f8).endVertex();
        wr.pos(-7.0D, 2.0D, 2.0D).tex(f7, f8).endVertex();
        wr.pos(-7.0D, -2.0D, 2.0D).tex(f7, f9).endVertex();
        wr.pos(-7.0D, -2.0D, -2.0D).tex(f6, f9).endVertex();
        Tessellator.getInstance().draw();

    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        renderVortex((EntityVortex) par1Entity, par2, par4, par6, par8, par9);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity) {
        return TEXTURE;
    }
}
