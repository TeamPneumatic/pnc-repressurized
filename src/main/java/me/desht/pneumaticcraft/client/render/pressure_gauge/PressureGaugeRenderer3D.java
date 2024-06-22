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

package me.desht.pneumaticcraft.client.render.pressure_gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D.*;

public class PressureGaugeRenderer3D {
    private static final float RADIUS = 19F;
    private static final float PI_F = (float) Math.PI;
    private static final float START_ANGLE = 240F / 180F * PI_F;
    private static final float STOP_ANGLE = -60F / 180F * PI_F;
    private static final int CIRCLE_POINTS = 180;
    private static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2F * PI_F) * CIRCLE_POINTS);

    public static void drawPressureGauge(PoseStack matrixStack, MultiBufferSource buffer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(matrixStack, buffer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
    }

    /**
     * Render a pressure gauge into the world.
     *
     * @param minPressure minimum pressure
     * @param maxPressure maximum pressure
     * @param dangerPressure danger pressure (red area)
     * @param minWorkingPressure min. working pressure
     * @param currentPressure current pressure (where needle points)
     * @param xPos x position
     * @param yPos y position
     * @param fgColor color to draw the surround, needle and text
     */
    public static void drawPressureGauge(PoseStack poseStack, MultiBufferSource buffer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        Matrix3f normal = poseStack.last().normal();

        // Draw the green and red surface in the gauge.
        RenderUtils.renderWithType(poseStack, buffer, ModRenderTypes.TRIANGLE_FAN, (posMat, builder) ->
                drawGaugeBackground(posMat, builder, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos));

        // Draw the surrounding circle in the foreground colour
        RenderUtils.renderWithType(poseStack, buffer, RenderType.LINE_STRIP, (posMat, builder) ->
                drawGaugeSurround(poseStack, builder, xPos, yPos, fgColor));

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        RenderUtils.renderWithType(poseStack, buffer, RenderType.LINES, (posMat, builder) ->
                drawScale(poseStack, builder, minPressure, maxPressure, xPos, yPos, currentScale, textScalers));

        // Draw the needle.
        RenderUtils.renderWithType(poseStack, buffer, RenderType.LINE_STRIP, (posMat, builder) -> {
                    float angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
                    angleIndicator = -angleIndicator / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
                    drawNeedle(poseStack, builder, xPos, yPos, angleIndicator, fgColor);
                });

        // draw the numbers next to the scaler.
        drawText(poseStack, buffer, xPos, yPos, fgColor, textScalers);
    }

    private static void drawGaugeBackground(Matrix4f posMat, VertexConsumer builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        // vertex builder is set up to draw GL_TRIANGLE_FAN
        float[] color = RED;

        builder.addVertex(posMat, xPos, yPos, 0f).setColor(0.5f, 0.5f, 0.1f, 1f);

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                builder.addVertex(posMat, xPos, yPos, 0f).setColor(color[0], color[1], color[2], color[3]);
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                builder.addVertex(posMat, xPos, yPos, 0f).setColor(color[0], color[1], color[2], color[3]);
                i--;
                changedColorYellow = true;
            }
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            builder.addVertex(posMat, Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, 0f)
                    .setColor(color[0], color[1], color[2], color[3])
                    ;
        }
    }

    private static final float[][] GAUGE_SURROUND = new float[CIRCLE_POINTS + 1][2];
    static {
        for (int i = 0; i <= CIRCLE_POINTS; i++) {
            float angle = (float) i / (float) CIRCLE_POINTS * 2F * PI_F;
            GAUGE_SURROUND[i][0] = Mth.cos(angle) * RADIUS;
            GAUGE_SURROUND[i][1] = Mth.sin(angle) * RADIUS;
        }
    }

    private static void drawGaugeSurround(PoseStack poseStack, VertexConsumer builder, int xPos, int yPos, int fgColor) {
        // vertex builder is set up for VertexMode.LINE_STRIP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            RenderUtils.normalLine(builder, poseStack,
                    GAUGE_SURROUND[i][0] + xPos, GAUGE_SURROUND[i][1] + yPos, 0f,
                    GAUGE_SURROUND[i + 1][0] + xPos, GAUGE_SURROUND[i + 1][1] + yPos, 0f,
                    cols[0], cols[1], cols[2], cols[3],
                    true);
        }
    }

    private static void drawScale(PoseStack poseStack, VertexConsumer builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        // vertex builder is set up for VertexMode.LINE
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle);
                float y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.3F), (int) (y * RADIUS * 1.3F)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                float x1 = x * RADIUS * r1 + xPos;
                float y1 = y * RADIUS * r1 + yPos;
                float x2 = x * RADIUS * r2 + xPos;
                float y2 = y * RADIUS * r2 + yPos;
                RenderUtils.normalLine(builder, poseStack, x1, y1, 0f, x2, y2, 0f, 1f, 0f, 0f, 0f, false);
            }
        }
    }

    private static void drawNeedle(PoseStack poseStack, VertexConsumer builder, int xPos, int yPos, float angle, int fgColor) {
        // vertex builder is set up for VertexMode.LINE_STRIP
        float[] cols = RenderUtils.decomposeColorF(fgColor);

        float x1 = Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos;
        float y1 = Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos;
        float x2 = Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos;
        float y2 = Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos;
        float x3 = Mth.cos(angle) * RADIUS * 0.8F + xPos;
        float y3 = Mth.sin(angle) * RADIUS * 0.8F + yPos;

        RenderUtils.normalLine(builder, poseStack, x1, y1, 0f, x2, y2, 0f, cols[0], cols[1], cols[2], cols[3], true);
        RenderUtils.normalLine(builder, poseStack, x2, y2, 0f, x3, y3, 0f, cols[0], cols[1], cols[2], cols[3], true);
        RenderUtils.normalLine(builder, poseStack, x3, y3, 0f, x1, y1, 0f, cols[0], cols[1], cols[2], cols[3], true);
        RenderUtils.normalLine(builder, poseStack, x1, y1, 0f, x2, y2, 0f, cols[0], cols[1], cols[2], cols[3], true);
    }

    private static void drawText(PoseStack matrixStack, MultiBufferSource buffer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                matrixStack.pushPose();
                matrixStack.translate(xPos + scaler.x() - 1.5, yPos + scaler.y() - 1.5, 0);
                matrixStack.scale(0.5f, 0.5f, 1f);
                RenderUtils.renderString3d(Component.literal(Integer.toString(scaler.pressure())), 0, 0, fgColor, matrixStack, buffer, false, false);
                matrixStack.popPose();
            }
        }
    }

}
