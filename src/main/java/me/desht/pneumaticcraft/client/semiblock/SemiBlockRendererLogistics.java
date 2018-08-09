package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.util.RenderUtils;
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
    private float lightMul = -1F;

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick) {
        int alpha = semiBlock.getAlpha();
        if (alpha == 0) return;
        if ((semiBlock.getWorld().getTotalWorldTime() & 0xf) == 0) lightMul = -1;  // recalculate light level every 16 ticks
        GlStateManager.disableTexture2D();
        if (alpha < 255) GlStateManager.enableBlend();
        if (lightMul < 0) lightMul = getLightMultiplier(semiBlock);
        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & semiBlock.getColor(), lightMul);
        AxisAlignedBB aabb = semiBlock.getWorld() != null ?
            semiBlock.getBlockState().getSelectedBoundingBox(semiBlock.getWorld(), semiBlock.getPos()) :
            new AxisAlignedBB(0 + FRAME_WIDTH, 0 + FRAME_WIDTH, 0 + FRAME_WIDTH, 1 - FRAME_WIDTH, 1 - FRAME_WIDTH, 1 - FRAME_WIDTH);

        if (semiBlock.getPos() != null)
            GlStateManager.translate(-semiBlock.getPos().getX(), -semiBlock.getPos().getY(), -semiBlock.getPos().getZ());

        renderFrame(aabb, FRAME_WIDTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    private float getLightMultiplier(SemiBlockLogistics semiBlock) {
        return Math.max(0.05F, Minecraft.getMinecraft().world.getLight(semiBlock.getPos()) / 15F);
    }

    public static void renderFrame(AxisAlignedBB aabb, double fw) {
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.maxZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.minY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.minY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ - fw), 0, 0, 0);

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);
    }

    private static void renderOffsetAABB(AxisAlignedBB aabb, double x, double y, double z) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);
        wr.setTranslation(x, y, z);

        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0, 0, -1).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(0, 0, -1).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0, 0, 1).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0, -1, 0).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0, -1, 0).endVertex();

        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0, 1, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0, 1, 0).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).normal(-1, 0, 0).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.minZ).normal(-1, 0, 0).endVertex();

        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(1, 0, 0).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(1, 0, 0).endVertex();
        wr.setTranslation(0.0D, 0.0D, 0.0D);
        Tessellator.getInstance().draw();
    }
}
