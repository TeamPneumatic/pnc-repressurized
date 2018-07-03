package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderTargetCircle {
    private double oldRotationAngle;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    private double rotationAcceleration = 0;
    private final Random rand;
    private boolean renderAsTagged;

    public RenderTargetCircle() {
        rand = new Random();
    }

    public void setRenderingAsTagged(boolean tagged) {
        renderAsTagged = tagged;
    }

    public void update() {
        oldRotationAngle = rotationAngle;
        if (rand.nextInt(15) == 0) rotationAcceleration = (rand.nextDouble() - 0.5D) / 2.5D;
        rotationSpeed += rotationAcceleration;// * 0.05D;
        double maxSpeed = 8.0D;
        if (rotationSpeed >= maxSpeed) rotationSpeed = maxSpeed;
        if (rotationSpeed <= -maxSpeed) rotationSpeed = -maxSpeed;
        rotationAngle += rotationSpeed;// * 0.05D;
    }

    public void render(double size, float partialTicks) {
        double renderRotationAngle = oldRotationAngle + (rotationAngle - oldRotationAngle) * partialTicks;
        BufferBuilder wr = Tessellator.getInstance().getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        // GlStateManager.lineWidth((float)size * 20F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GlStateManager.rotate((float) renderRotationAngle, 0, 0, 1);
        for (int j = 0; j < 2; j++) {
            wr.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION);
            for (int i = 0; i < PneumaticCraftUtils.circlePoints / 4; i++) {
                wr.pos(PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0).endVertex();
                wr.pos(PneumaticCraftUtils.cos[i] * (size + 0.1D), PneumaticCraftUtils.sin[i] * (size + 0.1D), 0).endVertex();
            }
            Tessellator.getInstance().draw();

            if (renderAsTagged) {
                GlStateManager.color(1, 0, 0, 1);
                wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
                for (int i = 0; i < PneumaticCraftUtils.circlePoints / 4; i++) {
                    wr.pos(PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0).endVertex();
                }
                for (int i = PneumaticCraftUtils.circlePoints / 4 - 1; i >= 0; i--) {
                    wr.pos(PneumaticCraftUtils.cos[i] * (size + 0.1D), PneumaticCraftUtils.sin[i] * (size + 0.1D), 0).endVertex();
                }
                Tessellator.getInstance().draw();
                GlStateManager.color(1, 1, 0, 0.5F);
            }

            GlStateManager.rotate(180, 0, 0, 1);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
}
