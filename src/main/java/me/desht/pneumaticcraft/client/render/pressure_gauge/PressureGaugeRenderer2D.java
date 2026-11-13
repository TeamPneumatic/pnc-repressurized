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

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.GuiVertexRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

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

    public static void drawPressureGauge(GuiGraphicsExtractor graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
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
    public static void drawPressureGauge(GuiGraphicsExtractor graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        float lineWidth = GuiVertexRenderState.pixels(1f);

        // Draw the green and red surface in the gauge.
        GuiVertexRenderState.submit(graphics, builder ->
                drawGaugeBackground(builder, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos));

        // Draw the surrounding circle in the foreground colour
        GuiVertexRenderState.submit(graphics, wr ->
                drawGaugeSurround(wr, xPos, yPos, fgColor, lineWidth));

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        GuiVertexRenderState.submit(graphics, wr ->
                drawScale(wr, minPressure, maxPressure, xPos, yPos, currentScale, textScalers, lineWidth));

        // Draw the needle.
        float angleIndicator0 = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        float angleIndicator = -angleIndicator0 / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
        GuiVertexRenderState.submit(graphics, wr ->
                drawNeedle(wr, xPos, yPos, angleIndicator, fgColor, lineWidth));

        // draw the numbers next to the scaler.
        drawText(graphics, fontRenderer, xPos, yPos, fgColor, textScalers);
    }

    private static void drawNeedle(GuiVertexRenderState.Builder builder, int xPos, int yPos, float angle, int fgColor, float lineWidth) {
        // line strip
        float[] xs = new float[] {
                Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos,
                Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos,
                Mth.cos(angle) * RADIUS * 0.8F + xPos,
                Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos
        };
        float[] ys = new float[] {
                Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos,
                Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos,
                Mth.sin(angle) * RADIUS * 0.8F + yPos,
                Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos
        };
        for (int i = 0; i < xs.length - 1; i++) {
            builder.line(xs[i], ys[i], xs[i + 1], ys[i + 1], lineWidth, fgColor);
        }
    }

    private static void drawScale(GuiVertexRenderState.Builder builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers, float lineWidth) {
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = (float) -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle);
                float y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.25D), (int) (y * RADIUS * 1.25D)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                builder.line(x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, lineWidth, 0xFF000000);
            }
        }
    }

    private static void drawText(GuiGraphicsExtractor graphics, Font fontRenderer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                graphics.pose().pushMatrix();
                graphics.pose().translate(xPos + scaler.x() - 1.5f, yPos + scaler.y() - 1.5f);
                graphics.pose().scale(0.5f, 0.5f);
                graphics.text(fontRenderer, Integer.toString(scaler.pressure()), 0, 0, GuiUtils.textColor(fgColor), false);
                graphics.pose().popMatrix();
            }
        }
    }

    private static void drawGaugeBackground(GuiVertexRenderState.Builder builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        // built as a triangle fan around the centre, emitted as triangles
        float[] color = RED;
        int centreColor = ARGB.colorFromFloat(1f, 0.5f, 0.5f, 0.1f);

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        List<FanVertex> fan = new ArrayList<>();
        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                fan.add(new FanVertex(xPos, yPos, toARGB(color)));
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                fan.add(new FanVertex(xPos, yPos, toARGB(color)));
                i--;
                changedColorYellow = true;
            }
            float angle = (float) -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            fan.add(new FanVertex(Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, toARGB(color)));
        }
        for (int i = 0; i < fan.size() - 1; i++) {
            FanVertex v1 = fan.get(i), v2 = fan.get(i + 1);
            builder.triangle(xPos, yPos, centreColor, v1.x(), v1.y(), v1.color(), v2.x(), v2.y(), v2.color());
        }
    }

    private static int toARGB(float[] color) {
        return ARGB.colorFromFloat(color[3], color[0], color[1], color[2]);
    }

    private static void drawGaugeSurround(GuiVertexRenderState.Builder builder, int xPos, int yPos, int fgColor, float lineWidth) {
        // line strip
        for (int i = 0; i < CIRCLE_POINTS - 1; i++) {
            float angle1 = (float) i / (float) CIRCLE_POINTS * 2F * PI_F;
            float angle2 = (float) (i + 1) / (float) CIRCLE_POINTS * 2F * PI_F;
            builder.line(Mth.cos(angle1) * RADIUS + xPos, Mth.sin(angle1) * RADIUS + yPos,
                    Mth.cos(angle2) * RADIUS + xPos, Mth.sin(angle2) * RADIUS + yPos, lineWidth, fgColor);
        }
    }

    private record FanVertex(float x, float y, int color) {
    }

    record TextScaler(int pressure, int x, int y) {
    }
}
