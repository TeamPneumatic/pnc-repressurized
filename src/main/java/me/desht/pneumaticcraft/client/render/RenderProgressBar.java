package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;

public class RenderProgressBar {
    public static void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, double minX, double minY, double maxX, double maxY, double zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        double x = MathHelper.lerp(progress / 100D, minX, maxX);

        // draw the bar
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getUntexturedQuad(true), (posMat, builder) -> {
            RenderUtils.posF(builder, posMat, minX, minY, zLevel).color(f1[0], f1[1], f1[2], f1[3]).lightmap(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, minX, minY + (maxY - minY), zLevel).color(f1[0], f1[1], f1[2], f1[3]).lightmap(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, x, minY + (maxY - minY), zLevel).color(f2[0], f2[1], f2[2], f2[3]).lightmap(FULL_BRIGHT).endVertex();
            RenderUtils.posF(builder, posMat, x, minY, zLevel).color(f2[0], f2[1], f2[2], f2[3]).lightmap(FULL_BRIGHT).endVertex();
        });

        // draw the outline
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getLineLoopsTransparent(1.5), (posMat, builder) -> {
            RenderUtils.posF(builder, posMat, minX, minY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, minX, maxY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, maxX, maxY, zLevel).color(0, 0, 0, 255).endVertex();
            RenderUtils.posF(builder, posMat, maxX, minY, zLevel).color(0, 0, 0, 255).endVertex();
        });
    }

    public static void render2d(MatrixStack matrixStack, float minX, float minY, float maxX, float maxY, float zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        float x = MathHelper.lerp(progress / 100F, minX, maxX);

        Matrix4f posMat = matrixStack.getLast().getMatrix();

        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        builder.pos(posMat, minX, minY, zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        builder.pos(posMat, minX, minY + (maxY - minY), zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        builder.pos(posMat, x, minY + (maxY - minY), zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();
        builder.pos(posMat, x, minY, zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();
        Tessellator.getInstance().draw();

        RenderSystem.lineWidth(2.0f);
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        builder.pos(posMat, minX, minY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.pos(posMat, minX, maxY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.pos(posMat, maxX, maxY, zLevel).color(0, 0, 0, 1f).endVertex();
        builder.pos(posMat, maxX, minY, zLevel).color(0, 0, 0, 1f).endVertex();
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }

    private static Pair<float[], float[]> calcColors(int color1, int color2, float progress) {
        float[] f1 = new TintColor(color1, true).getComponents(null);
        float[] f2;
        if (color1 != color2) {
            f2 = new TintColor(color2, true).getComponents(null);
            for (int i = 0; i < f1.length; i++) {
                f2[i] = MathHelper.lerp(progress / 100f, f1[i], f2[i]);
            }
            return Pair.of(f1, f2);
        } else {
            return Pair.of(f1, f1);
        }
    }
}
