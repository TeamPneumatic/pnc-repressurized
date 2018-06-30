package me.desht.pneumaticcraft.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderProgressBar {

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, int progress) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        double caseDistance = 0D;
        // draw the bar
        GL11.glColor4f(0, 1, 0, 0.7F);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * caseDistance, zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * (1D - caseDistance), zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * (1D - caseDistance), zLevel).endVertex();
        wr.pos(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * caseDistance, zLevel).endVertex();

        Tessellator.getInstance().draw();

        // draw the casing
        GL11.glColor4f(0, 0, 0, 1);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(minX, minY, zLevel).endVertex();
        wr.pos(minX, maxY, zLevel).endVertex();
        wr.pos(maxX, maxY, zLevel).endVertex();
        wr.pos(maxX, minY, zLevel).endVertex();
        Tessellator.getInstance().draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

    }
}
