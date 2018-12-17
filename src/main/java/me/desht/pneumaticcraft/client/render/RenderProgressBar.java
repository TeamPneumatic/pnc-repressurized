package me.desht.pneumaticcraft.client.render;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderProgressBar {

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, int progress) {
        render(minX, minY, maxX, maxY, zLevel, progress, 0xAA00FF00);
    }

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, int progress, int color) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        double caseDistance = 0D;
        // draw the bar
        RenderUtils.glColorHex(color);
//        GlStateManager.color(0, 1, 0, 0.7F);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * caseDistance, zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * (1D - caseDistance), zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * (1D - caseDistance), zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * caseDistance, zLevel).endVertex();

        Tessellator.getInstance().draw();

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
}
