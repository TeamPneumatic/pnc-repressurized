package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.function.BiConsumer;

import static net.minecraft.util.math.MathHelper.lerp;

public class RenderUtils {
    public static final int FULL_BRIGHT = 0x00F000F0;

    /**
     * Decompose a 32-bit color into ARGB 8-bit int values
     * @param color color to decompose
     * @return 4-element array of ints
     */
    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8  & 0xff;
        res[3] = color       & 0xff;
        return res;
    }

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[1] = (color >> 16 & 0xff) / 255f;
        res[2] = (color >> 8  & 0xff) / 255f;
        res[3] = (color       & 0xff) / 255f;
        return res;
    }

//    public static void render3DArrow() {
//        GlStateManager.disableTexture();
//        double arrowTipLength = 0.2;
//        double arrowTipRadius = 0.25;
//        double baseLength = 0.3;
//        double baseRadius = 0.15;
//
//        BufferBuilder wr = Tessellator.getInstance().getBuffer();
//        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
//        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
//            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
//            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
//            wr.pos(sin, 0, cos).endVertex();
//        }
//        Tessellator.getInstance().draw();
//        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
//        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
//            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
//            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
//            wr.pos(sin, baseLength, cos).endVertex();
//        }
//        Tessellator.getInstance().draw();
//        wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
//        for (int i = PneumaticCraftUtils.sin.length - 1; i >= 0; i--) {
//            double sin = PneumaticCraftUtils.sin[i] * baseRadius;
//            double cos = PneumaticCraftUtils.cos[i] * baseRadius;
//            wr.pos(sin, 0, cos).endVertex();
//            wr.pos(sin, baseLength, cos).endVertex();
//        }
//        Tessellator.getInstance().draw();
//
//        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
//        wr.pos(0, baseLength + arrowTipLength, 0).endVertex();
//        for (int i = 0; i < PneumaticCraftUtils.sin.length; i++) {
//            double sin = PneumaticCraftUtils.sin[i] * arrowTipRadius;
//            double cos = PneumaticCraftUtils.cos[i] * arrowTipRadius;
//            wr.pos(sin, baseLength, cos).endVertex();
//        }
//        wr.pos(0, baseLength, arrowTipRadius).endVertex();
//        Tessellator.getInstance().draw();
//        GlStateManager.enableTexture();
//    }

    public static void renderFrame(MatrixStack matrixStack, IRenderTypeBuffer buffer, AxisAlignedBB aabb, float fw, float r, float g, float b, float a, int packedLightIn, boolean disableDepthTest) {
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getBlockFrame(disableDepthTest));
        Matrix4f posMat = matrixStack.getLast().getMatrix();

        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.minZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.minZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX + fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.maxZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX + fw, aabb.maxY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.maxZ + fw), r, g, b, a, packedLightIn);

        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.minY + fw, aabb.maxZ - fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ - fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.minY + fw, aabb.maxZ - fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.maxX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ - fw), r, g, b, a, packedLightIn);

        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.minZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.minZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.minX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ + fw), r, g, b, a, packedLightIn);
        renderOffsetAABB(posMat, builder, new AxisAlignedBB(aabb.maxX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ + fw), r, g, b, a, packedLightIn);
    }

    private static void renderOffsetAABB(Matrix4f posMat, IVertexBuilder builder, AxisAlignedBB aabb, float r, float g, float b, float a, int packedLightIn) {
        float x1 = (float) aabb.minX;
        float y1 = (float) aabb.minY;
        float z1 = (float) aabb.minZ;
        float x2 = (float) aabb.maxX;
        float y2 = (float) aabb.maxY;
        float z2 = (float) aabb.maxZ;

        builder.pos(posMat, x1, y2, z1).color(r, g, b, a).normal(0, 0, -1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z1).color(r, g, b, a).normal(0, 0, -1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y1, z1).color(r, g, b, a).normal(0, 0, -1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y1, z1).color(r, g, b, a).normal(0, 0, -1).lightmap(packedLightIn).endVertex();

        builder.pos(posMat, x1, y1, z2).color(r, g, b, a).normal(0, 0, 1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y2, z2).color(r, g, b, a).normal(0, 0, 1).lightmap(packedLightIn).endVertex();

        builder.pos(posMat, x1, y1, z1).color(r, g, b, a).normal(0, -1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y1, z1).color(r, g, b, a).normal(0, -1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y1, z2).color(r, g, b, a).normal(0, -1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y1, z2).color(r, g, b, a).normal(0, -1, 0).lightmap(packedLightIn).endVertex();

        builder.pos(posMat, x1, y2, z2).color(r, g, b, a).normal(0, 1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z1).color(r, g, b, a).normal(0, 1, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y2, z1).color(r, g, b, a).normal(0, 1, 0).lightmap(packedLightIn).endVertex();

        builder.pos(posMat, x1, y1, z2).color(r, g, b, a).normal(-1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y2, z2).color(r, g, b, a).normal(-1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y2, z1).color(r, g, b, a).normal(-1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x1, y1, z1).color(r, g, b, a).normal(-1, 0, 0).lightmap(packedLightIn).endVertex();

        builder.pos(posMat, x2, y1, z1).color(r, g, b, a).normal(1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z1).color(r, g, b, a).normal(1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y2, z2).color(r, g, b, a).normal(1, 0, 0).lightmap(packedLightIn).endVertex();
        builder.pos(posMat, x2, y1, z2).color(r, g, b, a).normal(1, 0, 0).lightmap(packedLightIn).endVertex();
    }

    /**
     * Rotates the render matrix dependant on the rotation of a block. Used by many tile entity render methods.
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

    /**
     * Render a progressing line in GUI context
     * @param matrixStack
     * @param line the line to render
     * @param color line's colour
     */
    public static void renderProgressingLineGUI(MatrixStack matrixStack, ProgressingLine line, int color) {
        int[] cols = decomposeColor(color);
        float progress = line.getProgress();
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(posMat, line.startX, line.startY, line.startZ)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        wr.pos(posMat, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lerp(progress, line.startZ,line.endZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        Tessellator.getInstance().draw();
    }

    public static void renderProgressingLine(ProgressingLine line, MatrixStack matrixStack, IVertexBuilder builder, int color) {
        int[] cols = decomposeColor(color);
        float progress = line.getProgress();
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        posF(builder, posMat, line.startX, line.startY, line.startZ)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        posF(builder, posMat, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lerp(progress, line.startZ,line.endZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    public static void renderProgressingLine(ProgressingLine prev, ProgressingLine line, float partialTick, MatrixStack matrixStack, IVertexBuilder builder, int color) {
        int[] cols = decomposeColor(color);
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        float progress = line.getProgress();
        double lx1 = lerp(partialTick, line.startX, prev.startX);
        double ly1 = lerp(partialTick, line.startY, prev.startY);
        double lz1 = lerp(partialTick, line.startZ, prev.startZ);
        posF(builder, posMat, lx1, ly1, lz1)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        posF(builder, posMat,
                lerp(progress, lx1, lerp(partialTick, line.endX, prev.endX)),
                lerp(progress, ly1, lerp(partialTick, line.endY, prev.endY)),
                lerp(progress, lz1, lerp(partialTick, line.endZ, prev.endZ)))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    public static void renderRing(ProgressingLine line, ProgressingLine lastLine, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTick, float rotationYaw, float rotationPitch, int color) {
        matrixStackIn.push();

        double renderProgress = lerp(partialTick, lastLine.progress, line.progress);
        matrixStackIn.translate(
                (line.endX - line.startX) * renderProgress,
                (line.endY - line.startY) * renderProgress,
                (line.endZ - line.startZ) * renderProgress
        );
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotationYaw - 90));
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rotationPitch));

        IVertexBuilder builder = bufferIn.getBuffer(ModRenderTypes.getLineLoops(1.0));

        int[] cols = RenderUtils.decomposeColor(0xFF000000 | color);
        double size = (1 + 4 * renderProgress) / 16;
        Matrix4f posMat = matrixStackIn.getLast().getMatrix();
        for (int i = 0; i < PneumaticCraftUtils.CIRCLE_POINTS; i += 25) { // 25 sides is enough to look circular
            RenderUtils.posF(builder, posMat, 0f, PneumaticCraftUtils.sin[i] * size, PneumaticCraftUtils.cos[i] * size)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .endVertex();
        }
        matrixStackIn.pop();
    }

    /**
     * Rotate the matrix so that subsequent drawing is oriented toward the player's facing.  Useful for drawing HUD
     * elements in 3D space which need to face the player.
     *
     * @param matrixStack the matrix stack
     */
    public static void rotateToPlayerFacing(MatrixStack matrixStack) {
        matrixStack.rotate(Vector3f.YP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getYaw()));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getPitch()));
    }

    public static void drawTexture(MatrixStack matrixStack, IVertexBuilder builder, int x, int y, int packedLightIn) {
        drawTexture(matrixStack, builder, x, y, 0f, 0f, 1f, 1f, packedLightIn);
    }

    public static void drawTexture(MatrixStack matrixStack, IVertexBuilder builder, int x, int y, float u1, float v1, float u2, float v2, int packedLightIn) {
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        builder.pos(posMat, x, y + 16, 0)
                .color(1f, 1f, 1f, 1f)
                .tex(u1, v2)
                .lightmap(packedLightIn)
                .endVertex();
        builder.pos(posMat, x + 16, y + 16, 0)
                .color(1f, 1f, 1f, 1f)
                .tex(u2, v2)
                .lightmap(packedLightIn)
                .endVertex();
        builder.pos(posMat, x + 16, y, 0)
                .color(1f, 1f, 1f, 1f)
                .tex(u2, v1)
                .lightmap(packedLightIn)
                .endVertex();
        builder.pos(posMat, x, y, 0)
                .color(1f, 1f, 1f, 1f)
                .tex(u1, v1)
                .lightmap(packedLightIn)
                .endVertex();
    }

    /**
     * Convenience method to get double coords into {@link IVertexBuilder#pos(Matrix4f, float, float, float)}
     * @param builder the vertex builder
     * @param posMat the positioning matrix
     * @param x X
     * @param y Y
     * @param z Z
     * @return the vertex builder, for method chaining
     */
    public static IVertexBuilder posF(IVertexBuilder builder, Matrix4f posMat, double x, double y, double z) {
        return builder.pos(posMat, (float)x, (float)y, (float)z);
    }

    public static void finishBuffer(IRenderTypeBuffer buffer, RenderType type) {
        if (buffer instanceof IRenderTypeBuffer.Impl) {
            RenderSystem.disableDepthTest();
            ((IRenderTypeBuffer.Impl) buffer).finish(type);
        }
    }

    public static void renderWithType(MatrixStack matrixStack, IRenderTypeBuffer buffer, RenderType type, BiConsumer<Matrix4f, IVertexBuilder> consumer) {
        consumer.accept(matrixStack.getLast().getMatrix(), buffer.getBuffer(type));
        finishBuffer(buffer, type);
    }

    public static void renderString3d(String str, float x, float y, int color, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        fr.renderString(str, x, y, color, false, matrixStack.getLast().getMatrix(), buffer, false, 0, FULL_BRIGHT);
    }

    public static void renderString3d(String str, float x, float y, int color, MatrixStack matrixStack, IRenderTypeBuffer buffer, boolean dropShadow, boolean disableDepthTest) {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        fr.renderString(str, x, y, color, dropShadow, matrixStack.getLast().getMatrix(), buffer, disableDepthTest, 0, FULL_BRIGHT);
    }
}
