package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderLaser {

    private int ticksExisted;
    private final int coreColor, glowColor;

    public RenderLaser(int color) {
        this(color, color);
    }

    public RenderLaser(int coreColor, int glowColor) {
        this.coreColor = coreColor;
        this.glowColor = glowColor;
    }

    public void update() {
        ticksExisted++;
    }

    public void render(float partialTicks, double x1, double y1, double z1, double x2, double y2, double z2) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        double laserLength = PneumaticCraftUtils.distBetween(x1, y1, z1, x2, y2, z2);
        double laserSize = 0.4;

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.translated(x1, y1, z1);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        float f3 = MathHelper.sqrt(dx * dx + dz * dz);
        double rotYaw = Math.atan2(dx, dz) * 180.0D / Math.PI;
        double rotPitch = 90 - (float) (Math.atan2(dy, f3) * 180.0D / Math.PI);

        GlStateManager.rotated((float)rotYaw, 0, 1, 0);
        GlStateManager.rotated((float)rotPitch, 1, 0, 0);

        GlStateManager.scaled(laserSize, laserSize, laserSize);
        GlStateManager.translated(0, 0.6, 0);
        GlStateManager.rotated((ticksExisted + partialTicks) * 200, 0, 1, 0);

        GlStateManager.pushMatrix();
        GlStateManager.scaled(1, laserLength / laserSize, 1);

        /*   GlStateManager.translate(0, -0.01, 0);
           textureManager.bindTexture(Textures.RENDER_LASER_ANIMATION);
           renderAnimation(partialTicks, laserLength / laserSize);
           GlStateManager.translate(0, 0.01, 0);*/

        textureManager.bindTexture(Textures.RENDER_LASER);
        renderQuad(glowColor);
        textureManager.bindTexture(Textures.RENDER_LASER_OVERLAY);
        renderQuad(coreColor);
        GlStateManager.popMatrix();

        GlStateManager.rotated(180, 1, 0, 0);
        textureManager.bindTexture(Textures.RENDER_LASER_START);
        renderQuad(glowColor);
        textureManager.bindTexture(Textures.RENDER_LASER_START_OVERLAY);
        renderQuad(coreColor);

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        GlStateManager.color4f(1, 1, 1, 1);
    }

    private void renderQuad(int color) {
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        RenderUtils.glColorHex(color);
        wr.pos(-0.5, 0, 0).tex(0, 0).endVertex();
        wr.pos(-0.5, 1, 0).tex(0, 1).endVertex();
        wr.pos(0.5, 1, 0).tex(1, 1).endVertex();
        wr.pos(0.5, 0, 0).tex(1, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    /*  private void renderAnimation(float partialTicks, double length){
          float p = (ticksExisted + partialTicks) % 100 / 100;
          BufferBuilder wr = Tessellator.getInstance()getBuffer();
          wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
          t.addVertexWithUV(-0.5, 0, 0, 0, p);
          t.addVertexWithUV(-0.5, 1, 0, 0, length + p);
          t.addVertexWithUV(0.5, 1, 0, 1, length + p);
          t.addVertexWithUV(0.5, 0, 0, 1, p);
          Tessellator.getInstance().draw();
      }*/
}
