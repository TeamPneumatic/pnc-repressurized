package me.desht.pneumaticcraft.client.render.pressure_gauge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D.*;

public class PressureGaugeRenderer3D {
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
                    double angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
                    angleIndicator = -angleIndicator / CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
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
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            RenderUtils.posF(builder, posMat,Math.cos(angle) * RADIUS + xPos, Math.sin(angle) * RADIUS + yPos, 0.0)
                    .color(color[0], color[1], color[2], color[3])
                    .endVertex();
        }
    }

    private static void drawGaugeSurround(Matrix4f posMat, IVertexBuilder builder, int xPos, int yPos, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = (double) i / (double) CIRCLE_POINTS * 2D * Math.PI;
            RenderUtils.posF(builder, posMat, Math.cos(angle) * RADIUS + xPos, Math.sin(angle) * RADIUS + yPos, 0.0)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .endVertex();
        }
    }

    private static void drawScale(Matrix4f posMat, IVertexBuilder builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        // vertex builder is set up for GL_LINES
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                double x = Math.cos(angle);
                double y = Math.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.3D), (int) (y * RADIUS * 1.3D)));
                currentScale--;
                double r1 = maxPressure > 10 && textScalers.size() % 5 == 1 ? 0.8D : 0.92D;
                double r2 = maxPressure > 10 && textScalers.size() % 5 == 1 ? 1.15D : 1.08D;
                RenderUtils.posF(builder, posMat,x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, 0.0)
                        .color(0, 0, 0, 255)
                        .endVertex();
                RenderUtils.posF(builder, posMat,x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, 0.0)
                        .color(0, 0, 0, 255)
                        .endVertex();
            }
        }
    }

    private static void drawNeedle(Matrix4f posMat, IVertexBuilder builder, int xPos, int yPos, double angle, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        RenderUtils.posF(builder, posMat,Math.cos(angle + 0.89D * Math.PI) * RADIUS * 0.3D + xPos, Math.sin(angle + 0.89D * Math.PI) * RADIUS * 0.3D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        RenderUtils.posF(builder, posMat,Math.cos(angle + 1.11D * Math.PI) * RADIUS * 0.3D + xPos, Math.sin(angle + 1.11D * Math.PI) * RADIUS * 0.3D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        RenderUtils.posF(builder, posMat,Math.cos(angle) * RADIUS * 0.8D + xPos, Math.sin(angle) * RADIUS * 0.8D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    private static void drawText(MatrixStack matrixStack, IRenderTypeBuffer buffer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 11 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                matrixStack.push();
                matrixStack.translate(xPos + scaler.x - 1.5, yPos + scaler.y - 1.5, 0);
                matrixStack.scale(0.5f, 0.5f, 1f);
                RenderUtils.renderString3d(Integer.toString(scaler.pressure), 0, 0, fgColor, matrixStack, buffer, false, false);
                matrixStack.pop();
            }
        }
    }
}
