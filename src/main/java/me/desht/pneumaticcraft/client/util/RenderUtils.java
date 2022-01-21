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
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.function.BiConsumer;

import static net.minecraft.util.Mth.lerp;

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

    private static boolean drawSide(byte mask, Direction d1, Direction d2) {
        return ((mask & 1 << d1.get3DDataValue()) | (mask & 1 << d2.get3DDataValue())) != 0;
    }

    public static RenderType renderFrame(PoseStack matrixStack, MultiBufferSource buffer, AABB aabb, float fw, float r, float g, float b, float a, int packedLightIn, boolean disableDepthTest, Direction... sides) {
        RenderType type = ModRenderTypes.getBlockFrame(disableDepthTest);
        VertexConsumer builder = buffer.getBuffer(ModRenderTypes.getBlockFrame(disableDepthTest));
        Matrix4f posMat = matrixStack.last().pose();
        byte mask = 0;
        if (sides.length == 0) {
            mask = (byte) 0xFF;
        } else {
            for (Direction d: sides) mask |= 1 << d.get3DDataValue();
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
     * Rotates the render matrix dependant on the rotation of a block. Used by many tile entity render methods.
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
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90f));
                matrixStack.translate(0, -1, -1);
            }
            case DOWN -> {
                yRotation = 0;
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
                matrixStack.translate(0, -1, 1);
            }
            case NORTH -> yRotation = 0;
            case EAST -> yRotation = 90;
            case SOUTH -> yRotation = 180;
            default -> yRotation = 270;
        }
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
        return yRotation;
    }

    /**
     * Render a progressing line in GUI context
     * @param matrixStack the matrix stack
     * @param line the line to render
     * @param color line's colour
     */
    public static void renderProgressingLineGUI(PoseStack matrixStack, ProgressingLine line, int color, float lineWidth) {
        int[] cols = decomposeColor(color);
        float progress = line.getProgress();
        Matrix4f posMat = matrixStack.last().pose();
        BufferBuilder wr = Tesselator.getInstance().getBuilder();
        RenderSystem.lineWidth(lineWidth);
        wr.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        wr.vertex(posMat, line.startX, line.startY, line.startZ)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        wr.vertex(posMat, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lerp(progress, line.startZ,line.endZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        Tesselator.getInstance().end();
    }

    public static void renderProgressingLine(ProgressingLine line, PoseStack matrixStack, VertexConsumer builder, int color) {
        int[] cols = decomposeColor(color);
        float progress = line.getProgress();
        Matrix4f posMat = matrixStack.last().pose();
        posF(builder, posMat, line.startX, line.startY, line.startZ)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        posF(builder, posMat, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lerp(progress, line.startZ,line.endZ))
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    public static void renderProgressingLine(ProgressingLine prev, ProgressingLine line, float partialTick, PoseStack matrixStack, VertexConsumer builder, int color) {
        int[] cols = decomposeColor(color);
        Matrix4f posMat = matrixStack.last().pose();
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

    public static void renderRing(ProgressingLine line, ProgressingLine lastLine, PoseStack matrixStackIn, MultiBufferSource bufferIn, float partialTick, float rotationYaw, float rotationPitch, int color) {
        matrixStackIn.pushPose();

        double renderProgress = lerp(partialTick, lastLine.progress, line.progress);
        matrixStackIn.translate(
                (line.endX - line.startX) * renderProgress,
                (line.endY - line.startY) * renderProgress,
                (line.endZ - line.startZ) * renderProgress
        );
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotationYaw - 90));
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rotationPitch));

        VertexConsumer builder = bufferIn.getBuffer(ModRenderTypes.getLineLoops(1.0));

        int[] cols = RenderUtils.decomposeColor(0xFF000000 | color);
        double size = (1 + 4 * renderProgress) / 16;
        Matrix4f posMat = matrixStackIn.last().pose();
        for (int i = 0; i < PneumaticCraftUtils.CIRCLE_POINTS; i += 25) { // 25 sides is enough to look circular
            RenderUtils.posF(builder, posMat, 0f, PneumaticCraftUtils.sin[i] * size, PneumaticCraftUtils.cos[i] * size)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .endVertex();
        }
        matrixStackIn.popPose();
    }

    /**
     * Rotate the matrix so that subsequent drawing is oriented toward the player's facing.  Useful for drawing HUD
     * elements in 3D space which need to face the player.
     *
     * @param matrixStack the matrix stack
     */
    public static void rotateToPlayerFacing(PoseStack matrixStack) {
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180F - Minecraft.getInstance().gameRenderer.getMainCamera().getXRot()));
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
        if (buffer instanceof MultiBufferSource.BufferSource) {
            RenderSystem.disableDepthTest();
            ((MultiBufferSource.BufferSource) buffer).endBatch(type);
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

    public static void renderString3d(String str, float x, float y, int color, PoseStack matrixStack, MultiBufferSource buffer) {
        Font fr = Minecraft.getInstance().font;
        fr.drawInBatch(str, x, y, color, false, matrixStack.last().pose(), buffer, false, 0, FULL_BRIGHT);
    }

    public static void renderString3d(String str, float x, float y, int color, PoseStack matrixStack, MultiBufferSource buffer, boolean dropShadow, boolean disableDepthTest) {
        Font fr = Minecraft.getInstance().font;
        fr.drawInBatch(str, x, y, color, dropShadow, matrixStack.last().pose(), buffer, disableDepthTest, 0, FULL_BRIGHT);
    }
}
