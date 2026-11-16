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
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D.TextScaler;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
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

    public static void drawPressureGauge(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(poseStack, submitNodeCollector, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
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
    public static void drawPressureGauge(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        // Draw the green and red surface in the gauge.
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.TRIANGLES, (pose, builder) ->
                drawGaugeBackground(pose.pose(), builder, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos));

        // Work out the scale marks and labels now, since the geometry below is drawn later
        List<TextScaler> textScalers = new ArrayList<>();
        List<ScaleMark> scaleMarks = getScale(minPressure, maxPressure, xPos, yPos, textScalers);

        float angleIndicator = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        float needleAngle = -angleIndicator / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
        float lineWidth = RenderUtils.defaultLineWidth();

        // Draw the surrounding circle in the foreground colour, the scale and the needle
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, builder) -> {
            drawGaugeSurround(pose, builder, xPos, yPos, fgColor, lineWidth);
            drawScale(pose, builder, scaleMarks, lineWidth);
            drawNeedle(pose, builder, xPos, yPos, needleAngle, fgColor, lineWidth);
        });

        // draw the numbers next to the scaler.
        drawText(poseStack, submitNodeCollector, xPos, yPos, fgColor, textScalers);
    }

    private static void drawGaugeBackground(Matrix4f posMat, VertexConsumer builder, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        float[] color = RED;

        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);

        boolean changedColorGreen = false;
        boolean changedColorYellow = false;

        for (int i = 0; i < GAUGE_POINTS; i++) {
            if (i == explodeBoundary && !changedColorGreen) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? YELLOW : GREEN;
                changedColorGreen = true;
            }
            if (i == workingBoundary && !changedColorYellow) {
                color = minWorkingPressure < 0 && minWorkingPressure >= -1 ? GREEN : YELLOW;
                changedColorYellow = true;
            }
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            float angle2 = -(i + 1) / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;

            builder.addVertex(posMat, xPos, yPos, 0f).setColor(0.5f, 0.5f, 0.1f, 1f);
            
            builder.addVertex(posMat, Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, 0f)
                .setColor(color[0], color[1], color[2], color[3]);
            builder.addVertex(posMat, Mth.cos(angle2) * RADIUS + xPos, Mth.sin(angle2) * RADIUS + yPos, 0f)
            	.setColor(color[0], color[1], color[2], color[3]);
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

    private static void drawGaugeSurround(PoseStack.Pose pose, VertexConsumer builder, int xPos, int yPos, int fgColor, float lineWidth) {
        // vertex builder is set up for VertexMode.LINES
        float[] cols = RenderUtils.decomposeColorF(fgColor);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            RenderUtils.normalLine(builder, pose,
                    GAUGE_SURROUND[i][0] + xPos, GAUGE_SURROUND[i][1] + yPos, 0f,
                    GAUGE_SURROUND[i + 1][0] + xPos, GAUGE_SURROUND[i + 1][1] + yPos, 0f,
                    cols[0], cols[1], cols[2], cols[3],
                    lineWidth);
        }
    }

    private static List<ScaleMark> getScale(float minPressure, float maxPressure, int xPos, int yPos, List<TextScaler> textScalers) {
        List<ScaleMark> marks = new ArrayList<>();
        int currentScale = (int) maxPressure;
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle);
                float y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.3F), (int) (y * RADIUS * 1.3F)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                marks.add(new ScaleMark(x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos));
            }
        }
        return marks;
    }

    private static void drawScale(PoseStack.Pose pose, VertexConsumer builder, List<ScaleMark> scaleMarks, float lineWidth) {
        for (ScaleMark mark : scaleMarks) {
            RenderUtils.normalLine(builder, pose, mark.x1(), mark.y1(), 0f, mark.x2(), mark.y2(), 0f, 1f, 0f, 0f, 0f, lineWidth);
        }
    }

    private static void drawNeedle(PoseStack.Pose pose, VertexConsumer builder, int xPos, int yPos, float angle, int fgColor, float lineWidth) {
        float[] cols = RenderUtils.decomposeColorF(fgColor);

        float x1 = Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos;
        float y1 = Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos;

        float x2 = Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos;
        float y2 = Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos;

        float x3 = Mth.cos(angle) * RADIUS * 0.8F + xPos;
        float y3 = Mth.sin(angle) * RADIUS * 0.8F + yPos;

        // Edge 1: x1 -> x2
        RenderUtils.normalLine(builder, pose,
                x1, y1, 0f,
                x2, y2, 0f,
                cols[0], cols[1], cols[2], cols[3],
                lineWidth);

        // Edge 2: x2 -> x3
        RenderUtils.normalLine(builder, pose,
                x2, y2, 0f,
                x3, y3, 0f,
                cols[0], cols[1], cols[2], cols[3],
                lineWidth);

        // Edge 3: x3 -> x1
        RenderUtils.normalLine(builder, pose,
                x3, y3, 0f,
                x1, y1, 0f,
                cols[0], cols[1], cols[2], cols[3],
                lineWidth);
    }

    private static void drawText(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                poseStack.pushPose();
                poseStack.translate(xPos + scaler.x() - 1.5, yPos + scaler.y() - 1.5, 0);
                poseStack.scale(0.5f, 0.5f, 1f);
                FormattedCharSequence text = Component.literal(Integer.toString(scaler.pressure())).getVisualOrderText();
                submitNodeCollector.submitText(poseStack, 0, 0, text, false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, fgColor, 0, 0);
                poseStack.popPose();
            }
        }
    }

    private record ScaleMark(float x1, float y1, float x2, float y2) {
    }
}
