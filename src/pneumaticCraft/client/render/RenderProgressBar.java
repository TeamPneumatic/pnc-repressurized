package pneumaticCraft.client.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderProgressBar{

    public static void render(double minX, double minY, double maxX, double maxY, double zLevel, int progress){
        //float red = 0.5F;
        // float green = 0.5F;
        // float blue = 0.5F;
        //float alpha = 0.3F;

        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        // GL11.glEnable(GL11.GL_BLEND);
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        double caseDistance = 0D;
        // draw the bar
        // GL11.glLineWidth((float)(maxY - minY) * 1.95F);
        tessellator.startDrawing(GL11.GL_QUADS);
        // tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * caseDistance, zLevel);
        tessellator.addVertex(minX + (maxX - minX) * caseDistance, minY + (maxY - minY) * (1D - caseDistance), zLevel);
        tessellator.addVertex(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * (1D - caseDistance), zLevel);
        tessellator.addVertex(minX + (maxX - minX) * caseDistance + (maxX - minX) * (1D - 2 * caseDistance) * progress / 100D, minY + (maxY - minY) * caseDistance, zLevel);

        tessellator.draw();

        GL11.glColor4f(0, 0, 0, 1);
        // draw the casing.
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        // tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, zLevel);
        tessellator.addVertex(minX, maxY, zLevel);
        tessellator.addVertex(maxX, maxY, zLevel);
        tessellator.addVertex(maxX, minY, zLevel);
        tessellator.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();

    }
}
