package pneumaticCraft.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.entity.EntityRing;

public class RenderEntityRing extends Render{

    @Override
    public void doRender(Entity entity, double par2, double par4, double par6, float var8, float par9){
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        if(entity instanceof EntityRing) {
            EntityRing ring = (EntityRing)entity;
            if(ring.oldRing != null) {
                GL11.glColor4d(1, 1, 1, 1);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_TEXTURE_2D);

                ring.ring.renderInterpolated(ring.oldRing, par9, entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par9 - 90.0F, entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par9);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
            }
        }
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity var1){
        return null;
    }

}
