package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderEntityRing extends EntityRenderer<EntityRing> {

    public static final IRenderFactory<EntityRing> FACTORY = RenderEntityRing::new;

    private RenderEntityRing(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(EntityRing ring, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (ring.oldRing != null) {
            float yaw = MathHelper.lerp(partialTicks, ring.yRotO, ring.yRot);
            float pitch = MathHelper.lerp(partialTicks, ring.xRotO, ring.xRot);
            RenderUtils.renderRing(ring.ring, ring.oldRing, matrixStackIn, bufferIn, partialTicks, yaw, pitch, ring.color);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRing entity) {
        return null;
    }
}
