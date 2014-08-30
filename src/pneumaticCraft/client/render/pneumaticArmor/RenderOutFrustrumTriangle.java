package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderOutFrustrumTriangle{
    public static void renderTriangle(Entity entity){
        double playerYaw = -RenderManager.instance.playerViewY;
        while(playerYaw >= 360D) {
            playerYaw -= 360;
        }
        while(playerYaw < 0) {
            playerYaw += 360;
        }
        double angle = playerYaw * Math.sin(Math.toRadians(RenderManager.instance.viewerPosX));
        //  double angle = playerYaw;
        // System.out.println("viewY: " + RenderManager.instance.playerViewY);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        int middleX = sr.getScaledWidth() / 2;
        int middleY = sr.getScaledHeight() / 2;
        int triangleX;
        int triangleY;
        double switchAngle = Math.toDegrees(Math.atan((double)middleX / middleY));
        // System.out.println("angle: " + angle + ", switch angle: " + switchAngle);
        float triangleAngle = 0;
        int distanceFromEdge = 1;
        if(angle < switchAngle) {
            triangleY = distanceFromEdge;
            triangleX = middleX + (int)(Math.tan(Math.toRadians(angle)) * middleY);
        } else if(angle > 360 - switchAngle) {
            triangleY = distanceFromEdge;
            triangleX = middleX - (int)(Math.tan(Math.toRadians(360 - angle)) * middleY);
        } else if(angle < 180 - switchAngle) {
            triangleAngle = 90;
            triangleX = sr.getScaledWidth() - distanceFromEdge;
            triangleY = middleY - (int)(Math.tan(Math.toRadians(90 - angle)) * middleX);
        } else if(angle < 180 + switchAngle) {
            triangleAngle = 180;
            triangleY = sr.getScaledHeight() - distanceFromEdge;
            triangleX = middleX + (int)(Math.tan(Math.toRadians(180 - angle)) * middleY);
        } else {
            triangleAngle = 270;
            triangleX = distanceFromEdge;
            triangleY = middleY + (int)(Math.tan(Math.toRadians(270 - angle)) * middleX);
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        // GL11.glEnable(GL11.GL_BLEND);
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(2.0F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glTranslated(triangleX, triangleY, 0);
        GL11.glRotatef(triangleAngle, 0, 0, 1);
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        tessellator.addVertex(5, 5, -90F);
        tessellator.addVertex(15, 5, -90F);
        tessellator.addVertex(10, 0, -90F);
        tessellator.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
