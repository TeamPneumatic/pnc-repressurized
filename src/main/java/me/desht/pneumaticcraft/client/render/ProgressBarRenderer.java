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

package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.client.util.GuiVertexRenderState;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;

import static me.desht.pneumaticcraft.client.util.RenderUtils.normalLine;
import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public class ProgressBarRenderer {
    public static void render3d(PoseStack poseStack, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, float zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        float x = Mth.lerp(progress / 100F, minX, maxX);

        // draw the bar
        RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.UNTEXTURED_QUAD_NO_DEPTH, (posMat, builder) -> {
            builder.addVertex(posMat, minX, minY, zLevel).setColor(f1[0], f1[1], f1[2], f1[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, minX, minY + (maxY - minY), zLevel).setColor(f1[0], f1[1], f1[2], f1[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, x, minY + (maxY - minY), zLevel).setColor(f2[0], f2[1], f2[2], f2[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, x, minY, zLevel).setColor(f2[0], f2[1], f2[2], f2[3]).setLight(FULL_BRIGHT);
        });

        // draw the outline
        final Matrix3f normal = poseStack.last().normal();
        RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.getLineLoops(1.5), (posMat, builder) -> {
            normalLine(builder, poseStack, minX, minY, zLevel, minX, maxY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, minX, maxY, zLevel, maxX, maxY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, maxX, maxY, zLevel, maxX, minY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, maxX, minY, zLevel, minX, minY, zLevel, 255,0, 0, 0, true);
        });
    }

    public static void render2d(GuiGraphicsExtractor graphics, float minX, float minY, float maxX, float maxY, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        int c1 = ARGB.colorFromFloat(cols.getLeft()[3], cols.getLeft()[0], cols.getLeft()[1], cols.getLeft()[2]);
        int c2 = ARGB.colorFromFloat(cols.getRight()[3], cols.getRight()[0], cols.getRight()[1], cols.getRight()[2]);

        float x = Mth.lerp(progress / 100F, minX, maxX);
        float lineWidth = GuiVertexRenderState.pixels(2f);

        GuiVertexRenderState.submit(graphics, builder -> builder
                // the bar
                .vertex(minX, minY, c1)
                .vertex(minX, minY + (maxY - minY), c1)
                .vertex(x, minY + (maxY - minY), c2)
                .vertex(x, minY, c2)
                // the outline
                .line(minX, minY, minX, maxY, lineWidth, 0xFF000000)
                .line(minX, maxY, maxX, maxY, lineWidth, 0xFF000000)
                .line(maxX, maxY, maxX, minY, lineWidth, 0xFF000000)
        );
    }

    private static Pair<float[], float[]> calcColors(int color1, int color2, float progress) {
        float[] f1 = new TintColor(color1, true).getComponents(null);
        float[] f2;
        if (color1 != color2) {
            f2 = new TintColor(color2, true).getComponents(null);
            for (int i = 0; i < f1.length; i++) {
                f2[i] = Mth.lerp(progress / 100f, f1[i], f2[i]);
            }
            return Pair.of(f1, f2);
        } else {
            return Pair.of(f1, f1);
        }
    }
}
