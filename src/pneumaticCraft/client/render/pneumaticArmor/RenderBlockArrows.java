package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class RenderBlockArrows{
    public int ticksExisted;

    public void render(World world, int x, int y, int z, float partialTicks){
        // if(true) return;
        Block block = world.getBlock(x, y, z);
        block.setBlockBoundsBasedOnState(world, x, y, z);
        double minX = block.getBlockBoundsMinX();
        double minY = block.getBlockBoundsMinY();
        double minZ = block.getBlockBoundsMinZ();
        double maxX = block.getBlockBoundsMaxX();
        double maxY = block.getBlockBoundsMaxY();
        double maxZ = block.getBlockBoundsMaxZ();
        if(ticksExisted > 10) ticksExisted = 0;
        float progress = (ticksExisted + partialTicks) / 10F;
        GL11.glLineWidth(1.0F);
        GL11.glColor4d(1, 1, 1, progress);
        GL11.glPushMatrix();
        GL11.glTranslated(-0.5D, -0.5D, -0.5D);

        GL11.glPushMatrix();
        GL11.glTranslated(minX, minY, minZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(45, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, minY, minZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(45, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, minY, maxZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(45, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, minY, maxZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(45, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, maxY, minZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(135, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, maxY, minZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(135, 1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(minX, maxY, maxZ);
        GL11.glRotatef(45, 0, -1, 0);
        GL11.glRotatef(135, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(maxX, maxY, maxZ);
        GL11.glRotatef(45, 0, 1, 0);
        GL11.glRotatef(135, -1, 0, 0);
        drawArrow(progress);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    private void drawArrow(float progress){
        double arrowBaseWidth = 0.4D;
        double arrowBaseLength = 0.8D;
        double arrowLength = 1.5D;
        double arrowWidth = 0.7D;
        double scale = 0.1D;
        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, scale);
        GL11.glTranslatef(0, progress * 4, 0);
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_STRIP);
        tess.addVertex(-arrowBaseWidth, -arrowLength * 0.5D, 0);
        tess.addVertex(-arrowBaseWidth, -arrowLength * 0.5D + arrowBaseLength, 0);
        tess.addVertex(-arrowWidth, -arrowLength * 0.5D + arrowBaseLength, 0);
        tess.addVertex(0, arrowLength * 0.5D, 0);
        tess.addVertex(arrowWidth, -arrowLength * 0.5D + arrowBaseLength, 0);
        tess.addVertex(arrowBaseWidth, -arrowLength * 0.5D + arrowBaseLength, 0);
        tess.addVertex(arrowBaseWidth, -arrowLength * 0.5D, 0);
        tess.draw();
        GL11.glPopMatrix();
    }
}
