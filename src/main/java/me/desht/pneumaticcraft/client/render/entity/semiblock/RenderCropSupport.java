package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.entity.semiblock.CropSupportEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class RenderCropSupport extends RenderSemiblockBase<CropSupportEntity> {
    private final ModelCropSupport model;

    public RenderCropSupport(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelCropSupport(ctx.bakeLayer(PNCModelLayers.CROP_SUPPORT));
    }

    @Override
    public void render(CropSupportEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
        AABB aabb = entityIn.getBoundingBox();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, 0.8f, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180F));
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }
        matrixStackIn.scale((float)(aabb.maxX - aabb.minX), (float)(aabb.maxY - aabb.minY), (float)(aabb.maxZ - aabb.minZ));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.pack(0F, false), 0xFF544020);

        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CropSupportEntity entity) {
        return Textures.MODEL_CROP_SUPPORT;
    }
}
