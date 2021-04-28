package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderLogisticsFrame extends RenderSemiblockBase<EntityLogisticsFrame> {
    public static final IRenderFactory<EntityLogisticsFrame> FACTORY = RenderLogisticsFrame::new;

    private static final float FRAME_WIDTH = 1 / 32F;

    // TODO ridanisaurus uncomment
//    private final ModelLogisticsFrame model = new ModelLogisticsFrame();

    private RenderLogisticsFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityLogisticsFrame entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float alpha = entity.getAlpha() / 255F;
        if (alpha == 0f) return;

        float[] cols = RenderUtils.decomposeColorF(entity.getColor());

        matrixStackIn.push();

        matrixStackIn.translate(-0.5, 0, -0.5);
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        AxisAlignedBB aabb = entity.getBlockBounds().offset(entity.antiZfight, entity.antiZfight, entity.antiZfight);
        RenderUtils.renderFrame(matrixStackIn, bufferIn, aabb, FRAME_WIDTH, cols[1], cols[2], cols[3], alpha, packedLightIn | 0x00F00000, false, entity.getSide());

        matrixStackIn.pop();
    }

    // TODO ridanisaurus replace above method with this
//    @Override
//    public void render(EntityLogisticsFrame entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
//        float alpha = entity.getAlpha() / 255F;
//        if (alpha == 0f) return;
//
//        AxisAlignedBB aabb = entity.getBlockBounds().offset(entity.antiZfight, entity.antiZfight, entity.antiZfight);
//
//        matrixStackIn.push();
//        matrixStackIn.scale((float) aabb.getXSize(), (float) aabb.getYSize(), (float) aabb.getZSize());
//        matrixStackIn.translate(-0.5, 0, -0.5);
//        if (entity.getTimeSinceHit() > 0) {
//            wobble(entity, partialTicks, matrixStackIn);
//        }
//        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(getEntityTexture(entity)));
//        model.render(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.getPackedUV(0F, false), 1.0f, 1.0f, 1.0f, alpha);
//
//        matrixStackIn.pop();
//    }

    @Override
    public ResourceLocation getEntityTexture(EntityLogisticsFrame entityLogisticsFrame) {
        return null;
        // TODO ridanisaurus uncomment
//        return entityLogisticsFrame.getTexture();
    }
}
