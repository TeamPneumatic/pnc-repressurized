package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import me.desht.pneumaticcraft.common.heat.TemperatureCategory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class RenderHeatFrame extends RenderSemiblockBase<EntityHeatFrame> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[TemperatureCategory.values().length];
    static {
        for (TemperatureCategory tc : TemperatureCategory.values()) {
            TEXTURES[tc.getIndex()] = tc.getTextureName("heat_frame");
        }
    }

    private final ModelHeatFrame model;

    public RenderHeatFrame(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelHeatFrame(ctx.bakeLayer(PNCModelLayers.HEAT_FRAME));
    }

    @Override
    public void render(EntityHeatFrame entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        AABB aabb = entityIn.getBoundingBox();

        matrixStackIn.pushPose();
        matrixStackIn.scale((float) aabb.getXsize(), (float) aabb.getYsize(), (float) aabb.getZsize());
        matrixStackIn.translate(0, 1.5, 0);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180F));
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entityIn, packedLightIn), OverlayTexture.pack(0F, false), 0, 0, 0, 1);
        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHeatFrame entityHeatFrame) {
        TemperatureCategory tc = TemperatureCategory.forTemperature(entityHeatFrame.getSyncedTemperature());
        return TEXTURES[tc.getIndex()];
    }
}
