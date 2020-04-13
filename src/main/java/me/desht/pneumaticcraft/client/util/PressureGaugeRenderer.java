package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class PressureGaugeRenderer {
    private static final int CIRCLE_POINTS = 500;
    public static final double RADIUS = 20D;
    private static final double START_ANGLE = 240D / 180D * Math.PI;
    private static final double STOP_ANGLE = -60D / 180D * Math.PI;
    private static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2D * Math.PI) * CIRCLE_POINTS);

    private static final float[] RED = new float[] { 0.7f, 0f, 0f, 1f };
    private static final float[] GREEN = new float[] { 0f, 0.7f, 0f, 1f };
    private static final float[] YELLOW = new float[] { 0.9f, 0.9f, 0f, 1f };

    public static void drawPressureGauge(MatrixStack matrixStack, IVertexBuilder builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        // todo 1.15 in-world pressure gauge
    }

    public static void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
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
    public static void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(2.0F);

        BufferBuilder wr = Tessellator.getInstance().getBuffer();

        // Draw the green and red surface in the gauge.
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        drawGaugeBackground(wr, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos);
        Tessellator.getInstance().draw();

        // Draw the surrounding circle in the foreground colour
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        drawGaugeSurround(wr, xPos, yPos, fgColor);
        Tessellator.getInstance().draw();

        // Draw the scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        drawScale(wr, minPressure, maxPressure, xPos, yPos, currentScale, textScalers);
        Tessellator.getInstance().draw();

        // Draw the needle.
        double angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        angleIndicator = -angleIndicator / CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        drawNeedle(wr, xPos, yPos, angleIndicator, fgColor);
        Tessellator.getInstance().draw();

        RenderSystem.enableTexture();

        // draw the numbers next to the scaler.
        drawTextGUI(fontRenderer, xPos, yPos, fgColor, textScalers);
    }

    private static void drawNeedle(IVertexBuilder builder, int xPos, int yPos, double angle, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        builder.pos(Math.cos(angle + 0.89D * Math.PI) * RADIUS * 0.3D + xPos, Math.sin(angle + 0.89D * Math.PI) * RADIUS * 0.3D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        builder.pos(Math.cos(angle + 1.11D * Math.PI) * RADIUS * 0.3D + xPos, Math.sin(angle + 1.11D * Math.PI) * RADIUS * 0.3D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
        builder.pos(Math.cos(angle) * RADIUS * 0.8D + xPos, Math.sin(angle) * RADIUS * 0.8D + yPos, 0.0)
                .color(cols[1], cols[2], cols[3], cols[0])
                .endVertex();
    }

    private static void drawScale(IVertexBuilder builder, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        // vertex builder is set up for GL_LINES
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                double x = Math.cos(angle);
                double y = Math.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.3D), (int) (y * RADIUS * 1.3D)));
                currentScale--;
                builder.pos(x * RADIUS * 0.9D + xPos, y * RADIUS * 0.9D + yPos, 0.0).color(0, 0, 0, 1).endVertex();
                builder.pos(x * RADIUS * 1.1D + xPos, y * RADIUS * 1.1D + yPos, 0.0).color(0, 0, 0, 1).endVertex();
            }
        }
    }

    private static void drawTextGUI(FontRenderer fontRenderer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 11 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                RenderSystem.pushMatrix();
                RenderSystem.translated(xPos + scaler.x - 1.5, yPos + scaler.y - 1.5, 0);
                RenderSystem.scaled(0.5, 0.5, 1);
                fontRenderer.drawString(Integer.toString(scaler.pressure), 0, 0, fgColor);
                RenderSystem.popMatrix();
            }
        }
    }

    private static void drawGaugeBackground(IVertexBuilder builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        // vertex builder is set up to draw GL_TRIANGLE_FAN
        float[] color = RED;

        builder.pos(xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                builder.pos(xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();
                i--;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                builder.pos(xPos, yPos, 0.0).color(color[0], color[1], color[2], color[3]).endVertex();
                i--;
                changedColorYellow = true;
            }
            double angle = (double) -i / (double) CIRCLE_POINTS * 2D * Math.PI - STOP_ANGLE;
            builder.pos(Math.cos(angle) * RADIUS + xPos, Math.sin(angle) * RADIUS + yPos, 0.0)
                    .color(color[0], color[1], color[2], color[3])
                    .endVertex();
        }
    }

    private static void drawGaugeSurround(IVertexBuilder builder, int xPos, int yPos, int fgColor) {
        // vertex builder is set up for GL_LINE_LOOP
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angle = (double) i / (double) CIRCLE_POINTS * 2D * Math.PI;
            builder.pos(Math.cos(angle) * RADIUS + xPos, Math.sin(angle) * RADIUS + yPos, 0.0)
                    .color(cols[1], cols[2], cols[3], cols[0])
                    .endVertex();
        }
    }

    private static class TextScaler {
        final int pressure;
        final int x;
        final int y;

        private TextScaler(int pressure, int x, int y) {
            this.pressure = pressure;
            this.x = x;
            this.y = y;
        }
    }
}
