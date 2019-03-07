package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class SemiBlockRendererLogistics implements ISemiBlockRenderer<SemiBlockLogistics> {
    private static final double FRAME_WIDTH = 1 / 32D;
    private static final AxisAlignedBB DEFAULT_BOX = new AxisAlignedBB(
            0 + FRAME_WIDTH, 0 + FRAME_WIDTH, 0 + FRAME_WIDTH,
            1 - FRAME_WIDTH, 1 - FRAME_WIDTH, 1 - FRAME_WIDTH
    );

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick) {
        int alpha = semiBlock.getAlpha();
        if (alpha == 0) return;
        if (alpha < 255) GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & semiBlock.getColor(), getLightMultiplier(semiBlock));
        AxisAlignedBB aabb = semiBlock.getWorld() != null ?
                semiBlock.getBlockState().getBoundingBox(semiBlock.getWorld(), semiBlock.getPos()) : DEFAULT_BOX;
        RenderUtils.renderFrame(aabb, FRAME_WIDTH);
        drawSideHighlight(semiBlock, alpha, aabb);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawSideHighlight(SemiBlockLogistics semiBlock, int alpha, AxisAlignedBB aabb) {
        GlStateManager.enableBlend();
        RenderUtils.glColorHex((alpha / 2 << 24 | 0x00FFFFFF) & semiBlock.getColor(), getLightMultiplier(semiBlock));
        GlStateManager.glLineWidth(10.0F);
        double xMid = (aabb.minX + aabb.maxX) / 2;
        double yMid = (aabb.minY + aabb.maxY) / 2;
        double zMid = (aabb.minZ + aabb.maxZ) / 2;
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        switch (semiBlock.getSide()) {
            case DOWN:
                wr.pos(xMid, aabb.minY - 0.02, aabb.minZ).endVertex();
                wr.pos(xMid, aabb.minY - 0.02, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX, aabb.minY - 0.02, zMid).endVertex();
                wr.pos(aabb.minX, aabb.minY - 0.02, zMid).endVertex();
                break;
            case UP:
                wr.pos(xMid, aabb.maxY + 0.02, aabb.minZ).endVertex();
                wr.pos(xMid, aabb.maxY + 0.02, aabb.maxZ).endVertex();
                wr.pos(aabb.minX, aabb.maxY + 0.02, zMid).endVertex();
                wr.pos(aabb.maxX, aabb.maxY + 0.02, zMid).endVertex();
                break;
            case NORTH:
                wr.pos(aabb.minX, yMid, aabb.minZ - 0.02).endVertex();
                wr.pos(aabb.maxX, yMid, aabb.minZ - 0.02).endVertex();
                wr.pos(xMid, aabb.minY, aabb.minZ - 0.02).endVertex();
                wr.pos(xMid, aabb.maxY, aabb.minZ - 0.02).endVertex();
                break;
            case SOUTH:
                wr.pos(aabb.minX, yMid, aabb.maxZ + 0.02).endVertex();
                wr.pos(aabb.maxX, yMid, aabb.maxZ + 0.02).endVertex();
                wr.pos(xMid, aabb.minY, aabb.maxZ + 0.02).endVertex();
                wr.pos(xMid, aabb.maxY, aabb.maxZ + 0.02).endVertex();
                break;
            case WEST:
                wr.pos(aabb.minX - 0.02, yMid, aabb.minX).endVertex();
                wr.pos(aabb.minX - 0.02, yMid, aabb.maxZ).endVertex();
                wr.pos(aabb.minX - 0.02, aabb.minY, zMid).endVertex();
                wr.pos(aabb.minX - 0.02, aabb.maxY, zMid).endVertex();
                break;
            case EAST:
                wr.pos(aabb.maxX + 0.02, yMid, aabb.minX).endVertex();
                wr.pos(aabb.maxX + 0.02, yMid, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX + 0.02, aabb.minY, zMid).endVertex();
                wr.pos(aabb.maxX + 0.02, aabb.maxY, zMid).endVertex();
                break;
        }
        Tessellator.getInstance().draw();
    }

    private float getLightMultiplier(ISemiBlock semiBlock) {
        return ConfigHandler.client.semiBlockLighting ?
                Math.max(1, Minecraft.getMinecraft().world.getLight(semiBlock.getPos(), true)) / 15F :
                1F;
    }

}
