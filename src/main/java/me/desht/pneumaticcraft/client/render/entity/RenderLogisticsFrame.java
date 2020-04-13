package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderLogisticsFrame extends RenderSemiblockBase<EntityLogisticsFrame> {
    public static final IRenderFactory<EntityLogisticsFrame> FACTORY = RenderLogisticsFrame::new;

    private static final float FRAME_WIDTH = 1 / 32F;

    private RenderLogisticsFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityLogisticsFrame entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float alpha = entity.getAlpha() / 255F;
        if (alpha == 0f) return;
        if (alpha < 255f) GlStateManager.enableBlend();

        float[] cols = RenderUtils.decomposeColorF(entity.getColor());

        matrixStackIn.push();

        matrixStackIn.translate(-0.5, 0, -0.5);
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        AxisAlignedBB aabb = entity.getBlockBounds();
        RenderUtils.renderFrame(matrixStackIn, bufferIn, aabb, FRAME_WIDTH, cols[1], cols[2], cols[3], alpha, packedLightIn, false);
        drawSideHighlight(entity, aabb, partialTicks, matrixStackIn, bufferIn, cols[1], cols[2], cols[3], alpha);

        matrixStackIn.pop();
    }

    private void drawSideHighlight(EntityLogisticsFrame entity, AxisAlignedBB aabb, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float r, float g, float b, float a) {
        double d = 0.06 + 0.005 * MathHelper.sin((entity.ticksExisted + partialTicks) / 10f);
        double minX = aabb.minX + d;
        double minY = aabb.minY + d;
        double minZ = aabb.minZ + d;
        double maxX = aabb.maxX - d;
        double maxY = aabb.maxY - d;
        double maxZ = aabb.maxZ - d;

        double dist = Minecraft.getInstance().player.getDistanceSq(entity);
        RenderUtils.renderWithType(matrixStackIn, bufferIn,  ModRenderTypes.getLineLoops(dist > 64 ? (dist > 256 ? 1f : 2f) : 3f), (posMat, builder) -> {
            switch (entity.getFacing()) {
                case DOWN:
                    RenderUtils.posF(builder, posMat, minX, aabb.minY - 0.02, minZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, aabb.minY - 0.02, minZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, aabb.minY - 0.02, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, aabb.minY - 0.02, maxZ).color(r, g, b, a).endVertex();
                    break;
                case UP:
                    RenderUtils.posF(builder, posMat, minX, aabb.maxY + 0.02, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, aabb.maxY + 0.02, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, aabb.maxY + 0.02, minZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, aabb.maxY + 0.02, minZ).color(r, g, b, a).endVertex();
                    break;
                case NORTH:
                    RenderUtils.posF(builder, posMat, maxX, minY, aabb.minZ - 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, minY, aabb.minZ - 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, maxY, aabb.minZ - 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, maxY, aabb.minZ - 0.02).color(r, g, b, a).endVertex();
                    break;
                case SOUTH:
                    RenderUtils.posF(builder, posMat, maxX, maxY, aabb.maxZ + 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, maxY, aabb.maxZ + 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, minX, minY, aabb.maxZ + 0.02).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, maxX, minY, aabb.maxZ + 0.02).color(r, g, b, a).endVertex();
                    break;
                case WEST:
                    RenderUtils.posF(builder, posMat, aabb.minX - 0.02, minY, minZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.minX - 0.02, minY, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.minX - 0.02, maxY, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.minX - 0.02, maxY, minZ).color(r, g, b, a).endVertex();
                    break;
                case EAST:
                    RenderUtils.posF(builder, posMat, aabb.maxX + 0.02, maxY, minZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.maxX + 0.02, maxY, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.maxX + 0.02, minY, maxZ).color(r, g, b, a).endVertex();
                    RenderUtils.posF(builder, posMat, aabb.maxX + 0.02, minY, minZ).color(r, g, b, a).endVertex();
                    break;
            }
        });
    }

    @Override
    public ResourceLocation getEntityTexture(EntityLogisticsFrame entityLogisticsFrame) {
        return null;
    }
}
