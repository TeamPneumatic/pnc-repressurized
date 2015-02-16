package pneumaticCraft.client.model;

import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityCompressedIronBlock;
import pneumaticCraft.common.tileentity.TileEntityVortexTube;

public class ModelVortexTube extends BaseModel{

    public ModelVortexTube(){
        super("vortexTube.obj");
    }

    @Override
    protected void applyRenderPreps(TileEntity te){
        if(te instanceof TileEntityVortexTube) {
            int roll = ((TileEntityVortexTube)te).getRoll();
            GL11.glTranslated(0, -8, 0);
            GL11.glRotated(roll * -90, 0, 0, 1);
            GL11.glTranslated(0, 8, 0);
        }
        super.applyRenderPreps(te);
        GL11.glScaled(1 / 40D, 1 / 40D, 1 / 40D);
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        super.renderStatic(size, te);
        if(te instanceof TileEntityVortexTube) {
            if(((TileEntityVortexTube)te).shouldVisualize()) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glScaled(14, 14, 16);
                GL11.glTranslated(-8 / 16D, -17 / 16D, -0.5D);
                renderBox(19);
                GL11.glTranslated(0, 0, 14.01D / 16D);
                renderBox(0);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    @Override
    protected void renderAll(TileEntity te){
        if(te instanceof TileEntityVortexTube) {
            TileEntityVortexTube tube = (TileEntityVortexTube)te;
            int coldHeat = tube.getColdHeatLevel();
            int hotHeat = tube.getHotHeatLevel();
            model.renderAllExcept("Cold", "Hot_1", "Hot_2");

            double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(coldHeat);
            GL11.glColor4d(color[0], color[1], color[2], 1);
            model.renderOnly("Cold");

            color = TileEntityCompressedIronBlock.getColorForHeatLevel(hotHeat);
            GL11.glColor4d(color[0], color[1], color[2], 1);
            model.renderOnly("Hot_1", "Hot_2");

            GL11.glColor4d(1, 1, 1, 1);
        } else {
            super.renderAll(te);
        }

    }

    private void renderBox(int heatLevel){

        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 2 / 16D;

        double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
        GL11.glColor4d(color[0], color[1], color[2], (1 - color[1]) / 2);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);

        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);

        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(minX, minY, minZ);

        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);

        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);

        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glEnd();
    }
}
