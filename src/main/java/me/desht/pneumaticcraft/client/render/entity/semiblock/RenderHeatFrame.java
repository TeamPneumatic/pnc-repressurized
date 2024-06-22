package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.HeatFrameEntity;
import me.desht.pneumaticcraft.common.heat.TemperatureCategory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class RenderHeatFrame extends RenderSemiblockBase<HeatFrameEntity> {
    private final ModelHeatFrame model;

    public RenderHeatFrame(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelHeatFrame(ctx.bakeLayer(PNCModelLayers.HEAT_FRAME));
    }

    @Override
    public void render(HeatFrameEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        AABB aabb = entityIn.getBoundingBox();

        matrixStackIn.pushPose();
        matrixStackIn.scale((float) aabb.getXsize(), (float) aabb.getYsize(), (float) aabb.getZsize());
        matrixStackIn.translate(0, 1.5, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180F));
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entityIn, packedLightIn), OverlayTexture.pack(0F, false), 0xFF000000);
        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HeatFrameEntity heatFrameEntity) {
        return TemperatureCategory.forTemperature(heatFrameEntity.getSyncedTemperature()).getTextureLocation();
    }
}
