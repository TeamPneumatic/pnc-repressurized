package pneumaticCraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLaser{

    private int ticksExisted;
    private final int coreColor, glowColor;

    public RenderLaser(int color){
        this(color, color);
    }

    public RenderLaser(int coreColor, int glowColor){
        this.coreColor = coreColor;
        this.glowColor = glowColor;
    }

    public void update(){
        ticksExisted++;
    }

    public void render(float partialTicks, double x1, double y1, double z1, double x2, double y2, double z2){
        Minecraft mc = FMLClientHandler.instance().getClient();
        TextureManager textureManager = mc.renderEngine;

        double laserLength = PneumaticCraftUtils.distBetween(x1, y1, z1, x2, y2, z2);
        double laserSize = 0.4;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslated(x1, y1, z1);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        float f3 = MathHelper.sqrt_double(dx * dx + dz * dz);
        double rotYaw = Math.atan2(dx, dz) * 180.0D / Math.PI;
        double rotPitch = 90 - (float)(Math.atan2(dy, f3) * 180.0D / Math.PI);

        GL11.glRotated(rotYaw, 0, 1, 0);
        GL11.glRotated(rotPitch, 1, 0, 0);

        GL11.glScaled(laserSize, laserSize, laserSize);
        GL11.glTranslated(0, 0.6, 0);
        GL11.glRotated((ticksExisted + partialTicks) * 200, 0, 1, 0);

        GL11.glPushMatrix();
        GL11.glScaled(1, laserLength / laserSize, 1);

        /*   GL11.glTranslated(0, -0.01, 0);
           textureManager.bindTexture(Textures.RENDER_LASER_ANIMATION);
           renderAnimation(partialTicks, laserLength / laserSize);
           GL11.glTranslated(0, 0.01, 0);*/

        textureManager.bindTexture(Textures.RENDER_LASER);
        renderQuad(glowColor);
        textureManager.bindTexture(Textures.RENDER_LASER_OVERLAY);
        renderQuad(coreColor);
        GL11.glPopMatrix();

        GL11.glRotated(180, 1, 0, 0);
        textureManager.bindTexture(Textures.RENDER_LASER_START);
        renderQuad(glowColor);
        textureManager.bindTexture(Textures.RENDER_LASER_START_OVERLAY);
        renderQuad(coreColor);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glColor4d(1, 1, 1, 1);
    }

    private void renderQuad(int color){
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_I(color);
        t.addVertexWithUV(-0.5, 0, 0, 0, 0);
        t.addVertexWithUV(-0.5, 1, 0, 0, 1);
        t.addVertexWithUV(0.5, 1, 0, 1, 1);
        t.addVertexWithUV(0.5, 0, 0, 1, 0);
        t.draw();
    }

    /*  private void renderAnimation(float partialTicks, double length){
          float p = (ticksExisted + partialTicks) % 100 / 100;
          Tessellator t = Tessellator.instance;
          t.startDrawingQuads();
          t.addVertexWithUV(-0.5, 0, 0, 0, p);
          t.addVertexWithUV(-0.5, 1, 0, 0, length + p);
          t.addVertexWithUV(0.5, 1, 0, 1, length + p);
          t.addVertexWithUV(0.5, 0, 0, 1, p);
          t.draw();
      }*/
}
