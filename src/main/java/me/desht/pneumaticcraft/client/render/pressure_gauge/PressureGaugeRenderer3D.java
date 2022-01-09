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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

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

    public static void drawPressureGauge(MatrixStack matrixStack, IRenderTypeBuffer buffer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
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
    public static void drawPressureGauge(MatrixStack matrixStack, IRenderTypeBuffer buffer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        // Draw the green and red surface in the gauge.
        RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.TRIANGLE_FAN, (posMat, builder) ->
                drawGaugeBackground(posMat, builder, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos));

        // Draw the surrounding circle in the foreground colour
        RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.getLineLoops(2.0), (posMat, builder) ->
                drawGaugeSurround(posMat, builder, xPos, yPos, fgColor));

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        RenderUtils.renderWithType(matrixStack, buffer, RenderType.LINES, (posMat, builder) ->
                drawScale(posMat, builder, minPressure, maxPressure, xPos, yPos, currentScale, textScalers));

        // Draw the needle.
        RenderUtils.renderWithType(matrixStack, buffer, ModRenderTypes.getLineLoops(2.0), (posMat, builder) -> {
                    float angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
                    angleIndicator = -angleIndicator / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
                    drawNeedle(posMat, builder, xPos, yPos, angleIndicator, fgColor);
                });

        // draw the numbers next to the scaler.
        drawText(matrixStack, buffer, xPos, yPos, fgColor, textScalers);
    }

    private static void drawGaugeBackground(Matrix4f posMat, IVertexBuilder builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        // vertex builder is set up to draw GL_TRIANGLE_FAN
        float[] color = RED;

        RenderUtils.posF(builder, posMat, xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                RenderUtils.posF(builder, posMat, xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                RenderUtils.posF(builder, posMat, xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();
                i--;
                changedColorYellow = true;
            }
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            RenderUtils.posF(builder, posMat, MathHelper.cos(angle) * RADIUS + xPos, MathHelper.sin(angle) * RADIUS + yPos, 0.0)
                    .color(color[0], color[1], color[2], color[3])
                    .endVertex();
        }
    }

    private static void drawGaugeSurround(Matrix4f posMat, IVertexBuilder builder, int xPos, int yPos, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            float angle = (float) i / (float) CIRCLE_POINTS * 2F * PI_F;
            RenderUtils.posF(builder, posMat, MathHelper.cos(angle) * RADIUS + xPos, MathHelper.sin(angle) * RADIUS + yPos, 0.0)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .endVertex();
        }
    }

    private static void drawScale(Matrix4f posMat, IVertexBuilder builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        // vertex builder is set up for GL_LINES
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = MathHelper.cos(angle);
                float y = MathHelper.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.3F), (int) (y * RADIUS * 1.3F)));
                currentScale--;
                float r1 = maxPressure > 10 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 10 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                builder.vertex(posMat, x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, 0f)
                        .color(0, 0, 0, 255)
                        .endVertex();
                builder.vertex(posMat, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, 0f)
                        .color(0, 0, 0, 255)
                        .endVertex();
            }
        }
    }

    private static void drawNeedle(Matrix4f posMat, IVertexBuilder builder, int xPos, int yPos, float angle, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        builder.vertex(posMat, MathHelper.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos, MathHelper.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos, 0f)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        builder.vertex(posMat, MathHelper.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos, MathHelper.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos, 0f)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        builder.vertex(posMat, MathHelper.cos(angle) * RADIUS * 0.8F + xPos, MathHelper.sin(angle) * RADIUS * 0.8F + yPos, 0f)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    private static void drawText(MatrixStack matrixStack, IRenderTypeBuffer buffer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 11 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                matrixStack.pushPose();
                matrixStack.translate(xPos + scaler.x - 1.5, yPos + scaler.y - 1.5, 0);
                matrixStack.scale(0.5f, 0.5f, 1f);
                RenderUtils.renderString3d(Integer.toString(scaler.pressure), 0, 0, fgColor, matrixStack, buffer, false, false);
                matrixStack.popPose();
            }
        }
    }
}
