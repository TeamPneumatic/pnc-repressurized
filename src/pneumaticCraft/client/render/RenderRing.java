package pneumaticCraft.client.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RenderRing extends RenderProgressingLine{
    private final int color;

    public RenderRing(double startX, double startY, double startZ, double endX, double endY, double endZ, int color){
        super(startX, startY, startZ, endX, endY, endZ);
        this.color = color;
    }

    @SideOnly(Side.CLIENT)
    public void renderInterpolated(RenderProgressingLine lastTickLine, float partialTick, float rotationYaw, float rotationPitch){
        GL11.glPushMatrix();
        double renderProgress = getInter(progress, lastTickLine.progress, partialTick);
        GL11.glTranslated((getInter(endX, lastTickLine.endX, partialTick) - startX) * renderProgress, (getInter(endY, lastTickLine.endY, partialTick) - startY) * renderProgress, (getInter(endZ, lastTickLine.endZ, partialTick) - startZ) * renderProgress);
        GL11.glRotatef(rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rotationPitch, 0.0F, 0.0F, 1.0F);
        Tessellator tess = Tessellator.instance;
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorOpaque_I(color);
        double size = 5 / 16D;
        for(int i = 0; i < PneumaticCraftUtils.circlePoints; i++) {
            tess.addVertex(0, PneumaticCraftUtils.sin[i] * size, PneumaticCraftUtils.cos[i] * size);
        }
        tess.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }
}
