package pneumaticCraft.client.render.pneumaticArmor;

import java.util.Random;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.common.util.PneumaticCraftUtils;

public class RenderTargetCircle{
    private double oldRotationAngle;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    private double rotationAcceleration = 0;
    private final Random rand;
    private boolean renderAsTagged;

    public RenderTargetCircle(){
        rand = new Random();
    }

    public void setRenderingAsTagged(boolean tagged){
        renderAsTagged = tagged;
    }

    public void update(){
        oldRotationAngle = rotationAngle;
        if(rand.nextInt(15) == 0) rotationAcceleration = (rand.nextDouble() - 0.5D) / 2.5D;
        rotationSpeed += rotationAcceleration;// * 0.05D;
        double maxSpeed = 8.0D;
        if(rotationSpeed >= maxSpeed) rotationSpeed = maxSpeed;
        if(rotationSpeed <= -maxSpeed) rotationSpeed = -maxSpeed;
        rotationAngle += rotationSpeed;// * 0.05D;
    }

    public void render(double size, float partialTicks){
        double renderRotationAngle = oldRotationAngle + (rotationAngle - oldRotationAngle) * partialTicks;
        Tessellator tessellator = Tessellator.instance;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        // GL11.glLineWidth((float)size * 20F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glRotatef((float)renderRotationAngle, 0, 0, 1);
        tessellator.setNormal(0F, 1F, 0F);
        for(int j = 0; j < 2; j++) {
            tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
            for(int i = 0; i < PneumaticCraftUtils.circlePoints / 4; i++) {
                tessellator.addVertex(PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0);
                tessellator.addVertex(PneumaticCraftUtils.cos[i] * (size + 0.1D), PneumaticCraftUtils.sin[i] * (size + 0.1D), 0);
            }
            tessellator.draw();

            if(renderAsTagged) {
                GL11.glColor4d(1, 0, 0, 1);
                tessellator.startDrawing(GL11.GL_LINE_LOOP);
                for(int i = 0; i < PneumaticCraftUtils.circlePoints / 4; i++) {
                    tessellator.addVertex(PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0);
                }
                for(int i = PneumaticCraftUtils.circlePoints / 4 - 1; i >= 0; i--) {
                    tessellator.addVertex(PneumaticCraftUtils.cos[i] * (size + 0.1D), PneumaticCraftUtils.sin[i] * (size + 0.1D), 0);
                }
                tessellator.draw();
                GL11.glColor4d(1, 1, 0, 0.5);
            }

            GL11.glRotatef(180, 0, 0, 1);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
