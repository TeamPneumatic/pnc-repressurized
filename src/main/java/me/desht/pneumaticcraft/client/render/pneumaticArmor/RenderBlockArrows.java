package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderBlockArrows {
    public int ticksExisted;

    public void render(World world, BlockPos pos, float partialTicks) {
        // if(true) return;
        Block block = world.getBlockState(pos).getBlock();
//        block.setBlockBoundsBasedOnState(world, pos);
//        double minX = block.getBlockBoundsMinX();
//        double minY = block.getBlockBoundsMinY();
//        double minZ = block.getBlockBoundsMinZ();
//        double maxX = block.getBlockBoundsMaxX();
//        double maxY = block.getBlockBoundsMaxY();
//        double maxZ = block.getBlockBoundsMaxZ();
        AxisAlignedBB aabb = block.getBoundingBox(world.getBlockState(pos), world, pos);
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        if (ticksExisted > 10) ticksExisted = 0;
        float progress = (ticksExisted + partialTicks) / 10F;
        GL11.glLineWidth(1.0F);
        GL11.glColor4d(1, 1, 1, progress);
        GL11.glPushMatrix();
        GL11.glTranslated(-0.5D, -0.5D, -0.5D);

        GL11.glPushMatrix();
        GL11.glTranslated(minX, minY, minZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(45, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, minY, minZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(45, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, minY, maxZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(45, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, minY, maxZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(45, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, maxY, minZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(135, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, maxY, minZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(135, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, maxY, maxZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(135, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, maxY, maxZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(135, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    private void drawArrow(float progress) {
        double arrowBaseWidth = 0.4D;
        double arrowBaseLength = 0.8D;
        double arrowLength = 1.5D;
        double arrowWidth = 0.7D;
        double scale = 0.1D;
        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, scale);
        GL11.glTranslatef(0, progress * 4, 0);
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
        GL11.glPopMatrix();
    }
}
