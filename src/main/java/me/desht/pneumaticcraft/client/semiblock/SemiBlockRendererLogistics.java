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
        RenderUtils.glColorHex((alpha / 3 << 24 | 0x00FFFFFF) & semiBlock.getColor(), getLightMultiplier(semiBlock));
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        switch (semiBlock.getSide()) {
            case DOWN:
                wr.pos(aabb.minX, aabb.minY - 0.02, aabb.minZ).endVertex();
                wr.pos(aabb.maxX, aabb.minY - 0.02, aabb.minZ).endVertex();
                wr.pos(aabb.maxX, aabb.minY - 0.02, aabb.maxZ).endVertex();
                wr.pos(aabb.minX, aabb.minY - 0.02, aabb.maxZ).endVertex();
                break;
            case UP:
                wr.pos(aabb.minX, aabb.maxY + 0.02, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX, aabb.maxY + 0.02, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX, aabb.maxY + 0.02, aabb.minZ).endVertex();
                wr.pos(aabb.minX, aabb.maxY + 0.02, aabb.minZ).endVertex();
                break;
            case NORTH:
                wr.pos(aabb.maxX, aabb.minY, aabb.minZ - 0.02).endVertex();
                wr.pos(aabb.minX, aabb.minY, aabb.minZ - 0.02).endVertex();
                wr.pos(aabb.minX, aabb.maxY, aabb.minZ - 0.02).endVertex();
                wr.pos(aabb.maxX, aabb.maxY, aabb.minZ - 0.02).endVertex();
                break;
            case SOUTH:
                wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ + 0.02).endVertex();
                wr.pos(aabb.minX, aabb.maxY, aabb.maxZ + 0.02).endVertex();
                wr.pos(aabb.minX, aabb.minY, aabb.maxZ + 0.02).endVertex();
                wr.pos(aabb.maxX, aabb.minY, aabb.maxZ + 0.02).endVertex();
                break;
            case WEST:
                wr.pos(aabb.minX - 0.02, aabb.minY, aabb.minZ).endVertex();
                wr.pos(aabb.minX - 0.02, aabb.minY, aabb.maxZ).endVertex();
                wr.pos(aabb.minX - 0.02, aabb.maxY, aabb.maxZ).endVertex();
                wr.pos(aabb.minX - 0.02, aabb.maxY, aabb.minZ).endVertex();
                break;
            case EAST:
                wr.pos(aabb.maxX + 0.02, aabb.maxY, aabb.minZ).endVertex();
                wr.pos(aabb.maxX + 0.02, aabb.maxY, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX + 0.02, aabb.minY, aabb.maxZ).endVertex();
                wr.pos(aabb.maxX + 0.02, aabb.minY, aabb.minZ).endVertex();
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
