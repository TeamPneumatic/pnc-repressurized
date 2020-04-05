package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import static net.minecraft.util.math.MathHelper.lerp;

public class RenderUtils {
    public static void glColorHex(int color, float brightness) {
        float alpha = (color >> 24 & 255) / 255F;
        float div = 255F / brightness;
        float red = (color >> 16 & 255) / div;
        float green = (color >> 8 & 255) / div;
        float blue = (color & 255) / div;
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }

    /**
     * Decompose a 32-bit color into ARGB 8-bit int values
     * @param color color to decompose
     * @return 4-element array of ints
     */
    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[0] = color >> 16 & 0xff;
        res[1] = color >> 8  & 0xff;
        res[2] = color       & 0xff;
        return res;
    }

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[0] = (color >> 16 & 0xff) / 255f;
        res[1] = (color >> 8  & 0xff) / 255f;
        res[2] = (color       & 0xff) / 255f;
        return res;
    }

    public static void render3DArrow() {
        GlStateManager.disableTexture();
        double arrowTipLength = 0.2;
        double arrowTipRadius = 0.25;
        double baseLength = 0.3;
        double baseRadius = 0.15;

        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();
        wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
            wr.pos(sin, 0, cos).endVertex();
            wr.pos(sin, baseLength, cos).endVertex();
        }
        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(0, baseLength + arrowTipLength, 0).endVertex();
        for (int i = 0; i < PneumaticCraftUtils.sin.length; i++) {
            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
            wr.pos(sin, baseLength, cos).endVertex();
        }
        wr.pos(0, baseLength, arrowTipRadius).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture();
    }

    public static void renderFrame(AxisAlignedBB aabb, double fw) {
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.maxZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.maxZ + fw));

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.minY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.minY + fw, aabb.maxZ - fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ - fw));

        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.minZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ + fw));
        renderOffsetAABB(new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ + fw));
    }

    private static void renderOffsetAABB(AxisAlignedBB aabb) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);

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
        Tessellator.getInstance().draw();
    }

    /**
     * Rotates the render matrix dependant on the rotation of a block. Used in many render methods.
     *
     * @param matrixStack the matrix stack
     * @param facing block facing direction
     * @return the angle (in degrees) of resulting rotation around the Y axis
     */
    public static float rotateMatrixForDirection(MatrixStack matrixStack, Direction facing) {
        float yRotation;
        switch (facing) {
            case UP:
                yRotation = 0;
                matrixStack.rotate(Vector3f.XP.rotationDegrees(90f));
                matrixStack.translate(0, -1, -1);
                break;
            case DOWN:
                yRotation = 0;
                matrixStack.rotate(Vector3f.XP.rotationDegrees(-90f));
                matrixStack.translate(0, -1, 1);
                break;
            case NORTH:
                yRotation = 0;
                break;
            case EAST:
                yRotation = 90;
                break;
            case SOUTH:
                yRotation = 180;
                break;
            default:
                yRotation = 270;
                break;
        }
        matrixStack.rotate(Vector3f.YP.rotationDegrees(yRotation));
        return yRotation;
    }

    public static void renderRangeLines(RangeLines rangeLines, MatrixStack matrixStack, IVertexBuilder builder) {
        if (!rangeLines.shouldRender()) return;

        matrixStack.push();

        PlayerEntity player = ClientUtils.getClientPlayer();
        BlockPos pos = rangeLines.getPos();
        if (pos != null) {
            matrixStack.translate(pos.getX() - player.getPosX() + 0.5, pos.getY() - player.getPosY() + 0.5, pos.getZ() - player.getPosZ() + 0.5);
        }
        for (ProgressingLine line : rangeLines.getLines()) {
            renderProgressingLine(line, matrixStack, builder, rangeLines.getColor());
        }

        matrixStack.pop();
    }

    public static void renderProgressingLine(ProgressingLine line, MatrixStack matrixStack, IVertexBuilder builder, int color) {
        int[] cols = decomposeColor(color);
        double startX = line.startX;
        double startY = line.startY;
        double startZ = line.startZ;
        double endX = line.endX;
        double endY = line.endY;
        double endZ = line.endZ;
        float progress = line.getProgress();
        builder.pos(startX, startY, startZ).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        builder.pos(startX + (endX - startX) * progress, startY + (endY - startY) * progress, startZ + (endZ - startZ) * progress).endVertex();
    }

    public static void renderProgressingLine(ProgressingLine prev, ProgressingLine line, float partialTick, MatrixStack matrixStack, IVertexBuilder builder, int color) {
        int[] cols = decomposeColor(color);
        double startX = line.startX;
        double startY = line.startY;
        double startZ = line.startZ;
        double endX = line.endX;
        double endY = line.endY;
        double endZ = line.endZ;
        float progress = line.getProgress();
        builder.pos(lerp(partialTick, startX, prev.startX),
                lerp(partialTick, startY, prev.startY),
                lerp(partialTick, startZ, prev.startZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        builder.pos(
                lerp(partialTick, startX, prev.startX) + (lerp(partialTick, endX, prev.endX) - lerp(partialTick, startX, prev.startX)) * progress,
                lerp(partialTick, startY, prev.startY) + (lerp(partialTick, startY, prev.startY) - lerp(partialTick, endY, prev.endY)) * progress,
                lerp(partialTick, startZ, prev.startZ) + (lerp(partialTick, endZ, prev.endZ) - lerp(partialTick, startZ, prev.startZ)) * progress)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }
}
