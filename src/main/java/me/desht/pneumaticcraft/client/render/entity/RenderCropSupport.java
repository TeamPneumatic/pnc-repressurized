package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderCropSupport extends RenderSemiblockBase<EntityCropSupport> {
    public static final IRenderFactory<EntityCropSupport> FACTORY = RenderCropSupport::new;

    private final ModelCropSupport model = new ModelCropSupport();

    private RenderCropSupport(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EntityCropSupport entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
        AxisAlignedBB aabb = entityIn.getBoundingBox();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, 0.8f, 0);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }
        matrixStackIn.scale((float)(aabb.maxX - aabb.minX), (float)(aabb.maxY - aabb.minY), (float)(aabb.maxZ - aabb.minZ));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.getPackedUV(0F, false), 0.33f, 0.25f, 0.12f, 1F);

        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCropSupport entity) {
        return Textures.MODEL_CROP_SUPPORT;
    }
}
