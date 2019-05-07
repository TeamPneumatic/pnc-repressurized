package me.desht.pneumaticcraft.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderProgressBar {

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, float progress) {
        render(minX, minY, maxX, maxY, zLevel, progress, 0xAA37FD12);
    }

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, float progress, int color1, int color2) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        // draw the bar
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float[] f1 = new Color(color1, true).getComponents(null);
        float[] f2;
        if (color1 != color2) {
            f2 = new Color(color2, true).getComponents(null);
            float p = progress / 100f;
            f2[0] = f1[0] + (f2[0] - f1[0]) * p;
            f2[1] = f1[1] + (f2[1] - f1[1]) * p;
            f2[2] = f1[2] + (f2[2] - f1[2]) * p;
        } else {
            f2 = f1;
        }
        wr.pos(minX, minY, zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        wr.pos(minX, minY + (maxY - minY), zLevel).color(f1[0], f1[1], f1[2], f1[3]).endVertex();
        double x = minX + (maxX - minX) * progress / 100D;
        wr.pos(x, minY + (maxY - minY), zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();
        wr.pos(x, minY, zLevel).color(f2[0], f2[1], f2[2], f2[3]).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        // draw the casing
        GlStateManager.color(0, 0, 0, 1);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(minX, minY, zLevel).endVertex();
        wr.pos(minX, maxY, zLevel).endVertex();
        wr.pos(maxX, maxY, zLevel).endVertex();
        wr.pos(maxX, minY, zLevel).endVertex();
        Tessellator.getInstance().draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, float progress, int color) {
        render(minX, minY, maxX, maxY, zLevel, progress, color, color);

    }
}
