/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.BitSet;
import java.util.function.BiConsumer;

import static net.minecraft.util.Mth.lerp;

public class RenderUtils {
    public static final int FULL_BRIGHT = 0x00F000F0;
    private static final float FULL_CIRCLE = (float)(Math.PI * 2);
    private static final float STEP = FULL_CIRCLE / 25f;

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

    private static boolean drawSide(BitSet mask, Direction d1, Direction d2) {
        return mask.get(d1.get3DDataValue()) || mask.get(d2.get3DDataValue());
    }

    public static RenderType renderFrame(PoseStack matrixStack, MultiBufferSource buffer, AABB aabb, float fw, float r, float g, float b, float a, int packedLightIn, Direction... sides) {
        RenderType type = ModRenderTypes.BLOCK_FRAME;
        VertexConsumer builder = buffer.getBuffer(type);
        Matrix4f posMat = matrixStack.last().pose();
        BitSet mask = new BitSet(6);
        if (sides.length == 0) {
            mask.set(0, 6);
        } else {
            for (Direction d: sides) {
                mask.set(d.get3DDataValue());
            }
        }

        float x1 = (float) aabb.minX;
        float y1 = (float) aabb.minY;
        float z1 = (float) aabb.minZ;
        float x2 = (float) aabb.maxX;
        float y2 = (float) aabb.maxY;
        float z2 = (float) aabb.maxZ;

        if (drawSide(mask, Direction.DOWN, Direction.NORTH))
            renderOffsetAABB(posMat, builder, x1 + fw, y1 - fw, z1 - fw, x2 - fw, y1 + fw, z1 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.UP, Direction.NORTH))
            renderOffsetAABB(posMat, builder, x1 + fw, y2 - fw, z1 - fw, x2 - fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.DOWN, Direction.SOUTH))
            renderOffsetAABB(posMat, builder, x1 + fw, y1 - fw, z2 - fw, x2 - fw, y1 + fw, z2 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.UP, Direction.SOUTH))
            renderOffsetAABB(posMat, builder, x1 + fw, y2 - fw, z2 - fw, x2 - fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);

        if (drawSide(mask, Direction.DOWN, Direction.WEST))
            renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z1 + fw, x1 + fw, y1 + fw, z2 - fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.UP, Direction.WEST))
            renderOffsetAABB(posMat, builder, x1 - fw, y2 - fw, z1 + fw, x1 + fw, y2 + fw, z2 - fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.DOWN, Direction.EAST))
            renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z1 + fw, x2 + fw, y1 + fw, z2 - fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.UP, Direction.EAST))
            renderOffsetAABB(posMat, builder, x2 - fw, y2 - fw, z1 + fw, x2 + fw, y2 + fw, z2 - fw, r, g, b, a, packedLightIn);

        if (drawSide(mask, Direction.WEST, Direction.NORTH))
            renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z1 - fw, x1 + fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.EAST, Direction.NORTH))
            renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z1 - fw, x2 + fw, y2 + fw, z1 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.WEST, Direction.SOUTH))
            renderOffsetAABB(posMat, builder, x1 - fw, y1 - fw, z2 - fw, x1 + fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);
        if (drawSide(mask, Direction.EAST, Direction.SOUTH))
            renderOffsetAABB(posMat, builder, x2 - fw, y1 - fw, z2 - fw, x2 + fw, y2 + fw, z2 + fw, r, g, b, a, packedLightIn);

        return type;
    }

    private static void renderOffsetAABB(Matrix4f posMat, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int packedLightIn) {

        builder.vertex(posMat, x1, y2, z1).color(r, g, b, a).normal(0, 0, -1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z1).color(r, g, b, a).normal(0, 0, -1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y1, z1).color(r, g, b, a).normal(0, 0, -1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y1, z1).color(r, g, b, a).normal(0, 0, -1).uv2(packedLightIn).endVertex();

        builder.vertex(posMat, x1, y1, z2).color(r, g, b, a).normal(0, 0, 1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y2, z2).color(r, g, b, a).normal(0, 0, 1).uv2(packedLightIn).endVertex();

        builder.vertex(posMat, x1, y1, z1).color(r, g, b, a).normal(0, -1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y1, z1).color(r, g, b, a).normal(0, -1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y1, z2).color(r, g, b, a).normal(0, -1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y1, z2).color(r, g, b, a).normal(0, -1, 0).uv2(packedLightIn).endVertex();

        builder.vertex(posMat, x1, y2, z2).color(r, g, b, a).normal(0, 1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z1).color(r, g, b, a).normal(0, 1, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y2, z1).color(r, g, b, a).normal(0, 1, 0).uv2(packedLightIn).endVertex();

        builder.vertex(posMat, x1, y1, z2).color(r, g, b, a).normal(-1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y2, z2).color(r, g, b, a).normal(-1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y2, z1).color(r, g, b, a).normal(-1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x1, y1, z1).color(r, g, b, a).normal(-1, 0, 0).uv2(packedLightIn).endVertex();

        builder.vertex(posMat, x2, y1, z1).color(r, g, b, a).normal(1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z1).color(r, g, b, a).normal(1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y2, z2).color(r, g, b, a).normal(1, 0, 0).uv2(packedLightIn).endVertex();
        builder.vertex(posMat, x2, y1, z2).color(r, g, b, a).normal(1, 0, 0).uv2(packedLightIn).endVertex();
    }

    /**
     * Rotates the render matrix dependent on the rotation of a block. Used by many block entity render methods.
     *
     * @param matrixStack the matrix stack
     * @param facing block facing direction
     * @return the angle (in degrees) of resulting rotation around the Y axis
     */
    public static float rotateMatrixForDirection(PoseStack matrixStack, Direction facing) {
        float yRotation;
        switch (facing) {
            case UP -> {
                yRotation = 0;
                matrixStack.mulPose(Axis.XP.rotationDegrees(90f));
                matrixStack.translate(0, -1, -1);
            }
            case DOWN -> {
                yRotation = 0;
                matrixStack.mulPose(Axis.XP.rotationDegrees(-90f));
                matrixStack.translate(0, -1, 1);
            }
            case NORTH -> yRotation = 0;
            case EAST -> yRotation = 90;
            case SOUTH -> yRotation = 180;
            default -> yRotation = 270;
        }
        matrixStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        return yRotation;
    }

    public static void renderProgressingLine3d(ProgressingLine prev, ProgressingLine line, float partialTick, PoseStack matrixStack, VertexConsumer builder, int color) {
        float[] cols = decomposeColorF(color);
        float progress = line.getProgress();
        float lx1 = lerp(partialTick, line.startX, prev.startX);
        float ly1 = lerp(partialTick, line.startY, prev.startY);
        float lz1 = lerp(partialTick, line.startZ, prev.startZ);
        float lx2 = lerp(progress, lx1, lerp(partialTick, line.endX, prev.endX));
        float ly2 = lerp(progress, ly1, lerp(partialTick, line.endY, prev.endY));
        float lz2 = lerp(progress, lz1, lerp(partialTick, line.endZ, prev.endZ));
        normalLine(builder, matrixStack, lx1, ly1, lz1, lx2, ly2, lz2, cols[0], cols[1], cols[2], cols[3], false);
    }

    public static void renderRing(ProgressingLine line, ProgressingLine lastLine, PoseStack poseStack, MultiBufferSource bufferIn, float partialTick, float rotationYaw, float rotationPitch, int color) {
        poseStack.pushPose();

        double renderProgress = lerp(partialTick, lastLine.progress, line.progress);
        poseStack.translate(
                (line.endX - line.startX) * renderProgress,
                (line.endY - line.startY) * renderProgress,
                (line.endZ - line.startZ) * renderProgress
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationYaw - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationPitch));

        VertexConsumer builder = bufferIn.getBuffer(ModRenderTypes.getLineLoops(1.0));

        int[] cols = RenderUtils.decomposeColor(0xFF000000 | color);
        double size = (1 + 4 * renderProgress) / 16;
        Matrix4f posMat = poseStack.last().pose();
        for (float i = 0; i < FULL_CIRCLE; i += STEP) {
            Vec3 v1 = new Vec3(0, Mth.sin(i) * size, Mth.cos(i) * size);
            Vec3 v2 = new Vec3(0, Mth.sin(i + STEP) * size, Mth.cos(i + STEP) * size);
            RenderUtils.posF(builder, posMat, 0f, v1.y(), v1.z())
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .normal(poseStack.last(), 0f, (float) (v2.y() - v1.y()), (float) (v2.z() - v1.z()))
                    .endVertex();
        }
        poseStack.popPose();
    }

    /**
     * Rotate the matrix so that subsequent drawing is oriented toward the player's facing.  Useful for drawing HUD
     * elements in 3D space which need to face the player.
     *
     * @param matrixStack the matrix stack
     */
    public static void rotateToPlayerFacing(PoseStack matrixStack) {
        matrixStack.mulPose(Axis.YP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
        matrixStack.mulPose(Axis.XP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getMainCamera().getXRot()));
    }

    public static void drawTexture(PoseStack matrixStack, VertexConsumer builder, int x, int y, int packedLightIn) {
        drawTexture(matrixStack, builder, x, y, 0f, 0f, 1f, 1f, packedLightIn);
    }

    public static void drawTexture(PoseStack matrixStack, VertexConsumer builder, int x, int y, float u1, float v1, float u2, float v2, int packedLightIn) {
        Matrix4f posMat = matrixStack.last().pose();
        builder.vertex(posMat, x, y + 16, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(u1, v2)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x + 16, y + 16, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(u2, v2)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x + 16, y, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(u2, v1)
                .uv2(packedLightIn)
                .endVertex();
        builder.vertex(posMat, x, y, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(u1, v1)
                .uv2(packedLightIn)
                .endVertex();
    }

    /**
     * Convenience method to get double coords into {@link VertexConsumer#vertex(Matrix4f, float, float, float)}
     * @param builder the vertex builder
     * @param posMat the positioning matrix
     * @param x X
     * @param y Y
     * @param z Z
     * @return the vertex builder, for method chaining
     */
    public static VertexConsumer posF(VertexConsumer builder, Matrix4f posMat, double x, double y, double z) {
        return builder.vertex(posMat, (float)x, (float)y, (float)z);
    }

    public static void finishBuffer(MultiBufferSource buffer, RenderType type) {
        if (buffer instanceof MultiBufferSource.BufferSource mbs) {
            RenderSystem.disableDepthTest();
            mbs.endBatch(type);
        }
    }

    public static void renderWithTypeAndFinish(PoseStack matrixStack, MultiBufferSource buffer, RenderType type, BiConsumer<Matrix4f, VertexConsumer> consumer) {
        // use when drawing from RenderWorldLastEvent
        consumer.accept(matrixStack.last().pose(), buffer.getBuffer(type));
        finishBuffer(buffer, type);
    }

    public static void renderWithType(PoseStack matrixStack, MultiBufferSource buffer, RenderType type, BiConsumer<Matrix4f, VertexConsumer> consumer) {
        // use anywhere else (TER, entity renderer etc.)
        consumer.accept(matrixStack.last().pose(), buffer.getBuffer(type));
    }

    public static void renderString3d(Component str, float x, float y, int color, PoseStack matrixStack, MultiBufferSource buffer, boolean dropShadow, boolean disableDepthTest) {
        Font fr = Minecraft.getInstance().font;
        fr.drawInBatch(str, x, y, color, dropShadow, matrixStack.last().pose(), buffer, disableDepthTest ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
    }

    public static void renderString3d(FormattedCharSequence str, float x, float y, int color, PoseStack matrixStack, MultiBufferSource buffer, boolean dropShadow, boolean disableDepthTest) {
        Font fr = Minecraft.getInstance().font;
        fr.drawInBatch(str, x, y, color, dropShadow, matrixStack.last().pose(), buffer, disableDepthTest ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
    }

    public static void normalLine(VertexConsumer builder, PoseStack poseStack, float x1, float y1, float z1, float x2, float y2, float z2, float a, float r, float g, float b, boolean isStrip) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float d = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        builder.vertex(poseStack.last().pose(), x1, y1, z1)
                .color(r, g, b, a)
                .normal(poseStack.last(), nx / d , ny / d, nz / d)
                .endVertex();
        if (!isStrip) {
            // when drawing line strips, second set of x/y/z coords are just for normal calculation
            builder.vertex(poseStack.last(), x2, y2, z2)
                    .color(r, g, b, a)
                    .normal(poseStack.last(), nx / d , ny / d, nz / d)
                    .endVertex();
        }
    }
}
