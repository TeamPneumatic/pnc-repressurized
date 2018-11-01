package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderBlockArrows {
    public int ticksExisted;

    public void render(World world, BlockPos pos, float partialTicks) {
        IBlockState state = world.getBlockState(pos);
        AxisAlignedBB aabb = state.getBoundingBox(world, pos);
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        if (ticksExisted > 10) ticksExisted = 0;
        float progress = (ticksExisted + partialTicks) / 10F;
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.color(1, 1, 1, progress);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5D, -0.5D, -0.5D);

        GlStateManager.pushMatrix();
        GlStateManager.translate(minX, minY, minZ);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.rotate(45, 1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(maxX, minY, minZ);
        GlStateManager.rotate(45, 0, -1, 0);
        GlStateManager.rotate(45, 1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(minX, minY, maxZ);
        GlStateManager.rotate(45, 0, -1, 0);
        GlStateManager.rotate(45, -1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(maxX, minY, maxZ);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.rotate(45, -1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(minX, maxY, minZ);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.rotate(135, 1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(maxX, maxY, minZ);
        GlStateManager.rotate(45, 0, -1, 0);
        GlStateManager.rotate(135, 1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(minX, maxY, maxZ);
        GlStateManager.rotate(45, 0, -1, 0);
        GlStateManager.rotate(135, -1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(maxX, maxY, maxZ);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.rotate(135, -1, 0, 0);
        drawArrow(progress);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    private void drawArrow(float progress) {
        double arrowBaseWidth = 0.4D;
        double arrowBaseLength = 0.8D;
        double arrowLength = 1.5D;
        double arrowWidth = 0.7D;
        double scale = 0.1D;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, progress * 4, 0);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        wr.pos(-arrowBaseWidth, -arrowLength * 0.5D, 0).endVertex();
        wr.pos(-arrowBaseWidth, -arrowLength * 0.5D + arrowBaseLength, 0).endVertex();
        wr.pos(-arrowWidth, -arrowLength * 0.5D + arrowBaseLength, 0).endVertex();
        wr.pos(0, arrowLength * 0.5D, 0).endVertex();
        wr.pos(arrowWidth, -arrowLength * 0.5D + arrowBaseLength, 0).endVertex();
        wr.pos(arrowBaseWidth, -arrowLength * 0.5D + arrowBaseLength, 0).endVertex();
        wr.pos(arrowBaseWidth, -arrowLength * 0.5D, 0).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }
}
