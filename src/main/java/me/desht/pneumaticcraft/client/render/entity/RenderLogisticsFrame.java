package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderLogisticsFrame extends RenderSemiblockBase<EntityLogisticsFrame> {
    public static final IRenderFactory<EntityLogisticsFrame> FACTORY = RenderLogisticsFrame::new;

    private static final double FRAME_WIDTH = 1 / 32D;
    private static final AxisAlignedBB DEFAULT_BOX = new AxisAlignedBB(
            0 + FRAME_WIDTH, 0 + FRAME_WIDTH, 0 + FRAME_WIDTH,
            1 - FRAME_WIDTH, 1 - FRAME_WIDTH, 1 - FRAME_WIDTH
    );

    private RenderLogisticsFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void doRender(EntityLogisticsFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
        int alpha = entity.getAlpha();
        if (alpha == 0) return;
        if (alpha < 255) GlStateManager.enableBlend();

        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & entity.getColor());
//        AxisAlignedBB aabb = entity.getWorld() != null ? entity.getBoundingBox() : DEFAULT_BOX;

        GlStateManager.disableTexture();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x - 0.5, y, z - 0.5);
        if (entity.getTimeSinceHit() > 0) wobble(entity, partialTicks);

        AxisAlignedBB b = entity.getBlockBounds();
        RenderUtils.renderFrame(b, FRAME_WIDTH + entity.antiZfight);

        drawSideHighlight(entity, b, partialTicks);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    @Override
    public boolean isMultipass() {
        return false;
    }

    @Override
    public void renderMultipass(EntityLogisticsFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    private void drawSideHighlight(EntityLogisticsFrame entity, AxisAlignedBB aabb, float partialTicks) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        double dist = Minecraft.getInstance().player.getDistanceSq(entity);
        GlStateManager.lineWidth(dist > 64 ? (dist > 256 ? 1f : 2f) : 3f);
        GlStateManager.disableLighting();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        double d = 0.06 + 0.005 * MathHelper.sin((entity.ticksExisted + partialTicks) / 10f);
        double minX = aabb.minX + d;
        double minY = aabb.minY + d;
        double minZ = aabb.minZ + d;
        double maxX = aabb.maxX - d;
        double maxY = aabb.maxY - d;
        double maxZ = aabb.maxZ - d;

        switch (entity.getFacing()) {
            case DOWN:
                wr.pos(minX, aabb.minY - 0.02, minZ).endVertex();
                wr.pos(maxX, aabb.minY - 0.02, minZ).endVertex();
                wr.pos(maxX, aabb.minY - 0.02, maxZ).endVertex();
                wr.pos(minX, aabb.minY - 0.02, maxZ).endVertex();
                break;
            case UP:
                wr.pos(minX, aabb.maxY + 0.02, maxZ).endVertex();
                wr.pos(maxX, aabb.maxY + 0.02, maxZ).endVertex();
                wr.pos(maxX, aabb.maxY + 0.02, minZ).endVertex();
                wr.pos(minX, aabb.maxY + 0.02, minZ).endVertex();
                break;
            case NORTH:
                wr.pos(maxX, minY, aabb.minZ - 0.02).endVertex();
                wr.pos(minX, minY, aabb.minZ - 0.02).endVertex();
                wr.pos(minX, maxY, aabb.minZ - 0.02).endVertex();
                wr.pos(maxX, maxY, aabb.minZ - 0.02).endVertex();
                break;
            case SOUTH:
                wr.pos(maxX, maxY, aabb.maxZ + 0.02).endVertex();
                wr.pos(minX, maxY, aabb.maxZ + 0.02).endVertex();
                wr.pos(minX, minY, aabb.maxZ + 0.02).endVertex();
                wr.pos(maxX, minY, aabb.maxZ + 0.02).endVertex();
                break;
            case WEST:
                wr.pos(aabb.minX - 0.02, minY, minZ).endVertex();
                wr.pos(aabb.minX - 0.02, minY, maxZ).endVertex();
                wr.pos(aabb.minX - 0.02, maxY, maxZ).endVertex();
                wr.pos(aabb.minX - 0.02, maxY, minZ).endVertex();
                break;
            case EAST:
                wr.pos(aabb.maxX + 0.02, maxY, minZ).endVertex();
                wr.pos(aabb.maxX + 0.02, maxY, maxZ).endVertex();
                wr.pos(aabb.maxX + 0.02, minY, maxZ).endVertex();
                wr.pos(aabb.maxX + 0.02, minY, minZ).endVertex();
                break;
        }

        Tessellator.getInstance().draw();
        GlStateManager.enableLighting();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLogisticsFrame entityLogisticsFrame) {
        return null;
    }
}
