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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;

public class RenderProgressBar {
    public static void render3d(PoseStack matrixStack, MultiBufferSource buffer, double minX, double minY, double maxX, double maxY, double zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        double x = Mth.lerp(progress / 100D, minX, maxX);

        // draw the bar
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getUntexturedQuad(true), (posMat, builder) -> {
            RenderUtils.posF(builder, posMat, minX, minY, zLevel).color(f1[0], f1[1], f1[2], f1[3]).uv2(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, minX, minY + (maxY - minY), zLevel).color(f1[0], f1[1], f1[2], f1[3]).uv2(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, x, minY + (maxY - minY), zLevel).color(f2[0], f2[1], f2[2], f2[3]).uv2(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, x, minY, zLevel).color(f2[0], f2[1], f2[2], f2[3]).uv2(FULL_BRIGHT).endVertex();
        });

        // draw the outline
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getLineLoopsTransparent(1.5), (posMat, builder) -> {
            RenderUtils.posF(builder, posMat, minX, minY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, minX, maxY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, maxX, maxY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, maxX, minY, zLevel).color(0, 0, 0, 255).endVertex();
        });
    }

    public static void render2d(PoseStack matrixStack, float minX, float minY, float maxX, float maxY, float zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        float x = Mth.lerp(progress / 100F, minX, maxX);

        Matrix4f posMat = matrixStack.last().pose();

//        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(posMat, minX, minY, zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        builder.vertex(posMat, minX, minY + (maxY - minY), zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        builder.vertex(posMat, x, minY + (maxY - minY), zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();
        builder.vertex(posMat, x, minY, zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();
        Tesselator.getInstance().end();

        RenderSystem.lineWidth(2.0f);
        builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(posMat, minX, minY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.vertex(posMat, minX, maxY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.vertex(posMat, maxX, maxY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.vertex(posMat, maxX, minY, zLevel).color(0, 0, 0, 1f).endVertex();
        Tesselator.getInstance().end();
//        RenderSystem.shadeModel(GL11.GL_FLAT);
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
