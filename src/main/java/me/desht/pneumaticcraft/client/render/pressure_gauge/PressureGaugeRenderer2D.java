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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class PressureGaugeRenderer2D {
    static final int CIRCLE_POINTS = 500;
    static final float RADIUS = 20F;
    private static final float PI_F = (float) Math.PI;
    private static final float START_ANGLE = 240F / 180F * PI_F;
    static final float STOP_ANGLE = -60F / 180F * PI_F;
    static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2F * PI_F) * CIRCLE_POINTS);

    static final float[] RED = new float[] { 0.722f, 0.255f, 0.255f, 1f };
    static final float[] GREEN = new float[] { 0.475f, 0.678f, 0.224f, 1f };
    static final float[] YELLOW = new float[] { 0.922f, 0.635f, 0.239f, 1f };

    public static void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(graphics, fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
    }

    /**
     * Render a pressure gauge into a GUI.  Do not use for in-world rendering (e.g. pressure gauge module)
     *
     * @param fontRenderer the font renderer
     * @param minPressure minimum pressure
     * @param maxPressure maximum pressure
     * @param dangerPressure danger pressure (red area)
     * @param minWorkingPressure min. working pressure
     * @param currentPressure current pressure (where needle points)
     * @param xPos x position
     * @param yPos y position
     * @param fgColor color to draw the surround, needle and text
     */
    public static void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        RenderSystem.lineWidth(1.0F);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Draw the green and red surface in the gauge.
        RenderUtils.drawWithTesselator(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR, builder ->
                drawGaugeBackground(graphics, builder, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos));

        // Draw the surrounding circle in the foreground colour
        RenderUtils.drawWithTesselator(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR, wr ->
                drawGaugeSurround(graphics, wr, xPos, yPos, fgColor));

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        RenderUtils.drawWithTesselator(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR, wr ->
                drawScale(graphics, wr, minPressure, maxPressure, xPos, yPos, currentScale, textScalers));

        // Draw the needle.
        float angleIndicator0 = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        float angleIndicator = -angleIndicator0 / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
        RenderUtils.drawWithTesselator(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR, wr ->
                drawNeedle(graphics, wr, xPos, yPos, angleIndicator, fgColor));

        // draw the numbers next to the scaler.
        drawText(graphics, fontRenderer, xPos, yPos, fgColor, textScalers);
    }

    private static void drawNeedle(GuiGraphics graphics, VertexConsumer builder, int xPos, int yPos, float angle, int fgColor) {
        // VertexMode.DEBUG_LINE_STRIP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        Matrix4f posMat = graphics.pose().last().pose();
        builder.addVertex(posMat, Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos, Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos, 0F)
                .setColor(cols[1], cols[2], cols[3], cols[0]);
        builder.addVertex(posMat, Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos, Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos, 0F)
                .setColor(cols[1], cols[2], cols[3], cols[0]);
        builder.addVertex(posMat, Mth.cos(angle) * RADIUS * 0.8F + xPos, Mth.sin(angle) * RADIUS * 0.8F + yPos, 0.0F)
                .setColor(cols[1], cols[2], cols[3], cols[0]);
        builder.addVertex(posMat, Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos, Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos, 0F)
                .setColor(cols[1], cols[2], cols[3], cols[0]);
    }

    private static void drawScale(GuiGraphics graphics, VertexConsumer builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        // VertexMode.DEBUG_LINES
        Matrix4f posMat = graphics.pose().last().pose();
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = (float) -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle);
                float y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.25D), (int) (y * RADIUS * 1.25D)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                builder.addVertex(posMat, x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, 0F).setColor(0, 0, 0, 1);
                builder.addVertex(posMat, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, 0F).setColor(0, 0, 0, 1);
            }
        }
    }

    private static void drawText(GuiGraphics graphics, Font fontRenderer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                graphics.pose().pushPose();
                graphics.pose().translate(xPos + scaler.x() - 1.5, yPos + scaler.y() - 1.5, 0);
                graphics.pose().scale(0.5f, 0.5f, 1f);
                graphics.drawString(fontRenderer, Integer.toString(scaler.pressure()), 0, 0, fgColor, false);
                graphics.pose().popPose();
            }
        }
    }

    private static void drawGaugeBackground(GuiGraphics graphics, VertexConsumer builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        // vertex builder is set up to draw GL_TRIANGLE_FAN
        float[] color = RED;

        Matrix4f posMat = graphics.pose().last().pose();
        builder.addVertex(posMat, xPos, yPos, 0f).setColor(0.5f, 0.5f, 0.1f, 1f);

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                builder.addVertex(posMat, xPos, yPos, 0F).setColor(color[0], color[1], color[2], color[3]);
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                builder.addVertex(posMat, xPos, yPos, 0F).setColor(color[0], color[1], color[2], color[3]);
                i--;
                changedColorYellow = true;
            }
            float angle = (float) -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            builder.addVertex(posMat, Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, 0F)
                    .setColor(color[0], color[1], color[2], color[3]);
        }
    }

    private static void drawGaugeSurround(GuiGraphics graphics, VertexConsumer builder, int xPos, int yPos, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        Matrix4f posMat = graphics.pose().last().pose();
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            float angle = (float) i / (float) CIRCLE_POINTS * 2F * PI_F;
            builder.addVertex(posMat, Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, 0F)
                    .setColor(cols[1], cols[2], cols[3], cols[0]);
        }
    }

    record TextScaler(int pressure, int x, int y) {
    }
}
