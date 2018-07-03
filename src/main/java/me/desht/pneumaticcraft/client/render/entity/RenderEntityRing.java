package me.desht.pneumaticcraft.client.render.entity;

import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

public class RenderEntityRing extends Render<EntityRing> {

    public static final IRenderFactory<EntityRing> FACTORY = RenderEntityRing::new;

    public RenderEntityRing(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(@Nonnull EntityRing ring, double par2, double par4, double par6, float var8, float par9) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) par2, (float) par4, (float) par6);
        if (ring.oldRing != null) {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();

            ring.ring.renderInterpolated(ring.oldRing, par9, ring.prevRotationYaw + (ring.rotationYaw - ring.prevRotationYaw) * par9 - 90.0F, ring.prevRotationPitch + (ring.rotationPitch - ring.prevRotationPitch) * par9);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRing var1) {
        return null;
    }

}
