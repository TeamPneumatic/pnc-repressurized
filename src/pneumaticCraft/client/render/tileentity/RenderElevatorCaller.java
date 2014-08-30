package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityElevatorCaller;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class RenderElevatorCaller extends TileEntitySpecialRenderer{

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f){
        TileEntityElevatorCaller tile = (TileEntityElevatorCaller)tileentity;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glScalef(1.0F, -1F, -1F);
        // GL11.glRotated(180, 0, 0, 1);
        PneumaticCraftUtils.rotateMatrixByMetadata(tileentity.getBlockMetadata());
        GL11.glTranslatef(-1, 0, -1);

        for(TileEntityElevatorCaller.ElevatorButton button : tile.getFloors()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            tess.startDrawingQuads();
            tess.setColorRGBA_F(button.red, button.green, button.blue, 1F);
            tess.addVertex(button.posX + 0.5D, button.posY + 0.5D, 0.499D);
            tess.addVertex(button.posX + 0.5D, button.posY + button.height + 0.5D, 0.499D);
            tess.addVertex(button.posX + button.width + 0.5D, button.posY + button.height + 0.5D, 0.499D);
            tess.addVertex(button.posX + button.width + 0.5D, button.posY + 0.5D, 0.499D);
            tess.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);

            GL11.glPushMatrix();
            GL11.glTranslated(button.posX + 0.5D, button.posY + 0.5D, 0.498);
            GL11.glTranslated(button.width / 2, button.height / 2, 0);
            float textScale = Math.min((float)button.width / 10F, (float)button.height / 10F);
            GL11.glScalef(textScale, textScale, textScale);
            func_147498_b().drawString(button.buttonText, -func_147498_b().getStringWidth(button.buttonText) / 2, -func_147498_b().FONT_HEIGHT / 2, 0xFF000000);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
}
