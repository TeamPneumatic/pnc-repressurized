package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class SemiBlockRendererLogistics implements ISemiBlockRenderer<SemiBlockLogistics> {

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick) {
        int alpha = semiBlock.getAlpha();
        if (alpha == 0) return;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glColor4d(1, 0, 0, 1);
        if (alpha < 255) GL11.glEnable(GL11.GL_BLEND);
        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & semiBlock.getColor());
        double fw = 1 / 32D;
        AxisAlignedBB aabb;
        if (semiBlock.getWorld() != null) {
//            semiBlock.getBlockState().getBlock().setBlockBoundsBasedOnState(semiBlock.getWorld(), semiBlock.getPos());
            aabb = semiBlock.getBlockState().getBlock().getSelectedBoundingBox(semiBlock.getBlockState(), semiBlock.getWorld(), semiBlock.getPos());
        } else {
            aabb = new AxisAlignedBB(0 + fw, 0 + fw, 0 + fw, 1 - fw, 1 - fw, 1 - fw);
        }

        if (semiBlock.getPos() != null)
            GL11.glTranslated(-semiBlock.getPos().getX(), -semiBlock.getPos().getY(), -semiBlock.getPos().getZ());

        renderFrame(aabb, fw);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4d(1, 1, 1, 1);
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

    public static void renderOffsetAABB(AxisAlignedBB p_76978_0_, double p_76978_1_, double p_76978_3_, double p_76978_5_) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);
        wr.setTranslation(p_76978_1_, p_76978_3_, p_76978_5_);

        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ).normal(0, 0, -1).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ).normal(0, 0, -1).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ).normal(0, 0, -1).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ).normal(0, 0, -1).endVertex();

        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(0, 0, 1).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(0, 0, 1).endVertex();

        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ).normal(0, -1, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ).normal(0, -1, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ).normal(0, -1, 0).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ).normal(0, -1, 0).endVertex();

        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(0, 1, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ).normal(0, 1, 0).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ).normal(0, 1, 0).endVertex();

        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(-1, 0, 0).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ).normal(-1, 0, 0).endVertex();
        wr.pos(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ).normal(-1, 0, 0).endVertex();

        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ).normal(1, 0, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ).normal(1, 0, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ).normal(1, 0, 0).endVertex();
        wr.pos(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ).normal(1, 0, 0).endVertex();
        wr.setTranslation(0.0D, 0.0D, 0.0D);
        Tessellator.getInstance().draw();
    }
}
