package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class RenderNavigator{
    private final int targetX;
    private final int targetY;
    private final int targetZ;
    private final World worldObj;
    private PathEntity path;
    private boolean increaseAlpha;
    private double alphaValue = 0.2D;

    public RenderNavigator(World world, int targetX, int targetY, int targetZ){
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        worldObj = world;
        updatePath();
    }

    public void updatePath(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        path = worldObj.getEntityPathToXYZ(player, targetX, targetY, targetZ, CoordTrackUpgradeHandler.SEARCH_RANGE, true, true, false, true);
        if(!tracedToDestination()) {
            path = CoordTrackUpgradeHandler.getDronePath(player, targetX, targetY, targetZ);
        }
    }

    public void render(boolean wirePath, boolean xRayEnabled, float partialTicks){
        if(path == null) return;

        GL11.glDepthMask(false);
        if(xRayEnabled) GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(5.0F);

        boolean noDestinationPath = !tracedToDestination();

        Tessellator tess = Tessellator.instance;

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0.01D, 0);

        //Draws just wires
        if(wirePath) {
            if(noDestinationPath) {
                GL11.glEnable(GL11.GL_LINE_STIPPLE);
                GL11.glLineStipple(4, (short)0x00FF);
            }
            for(int i = 1; i < path.getCurrentPathLength(); i++) {
                double red = 1;
                if(path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005D;
                }
                GL11.glColor4d(red, 1 - red, 0, 0.5D);
                PathPoint lastPoint = path.getPathPointFromIndex(i - 1);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                tess.startDrawing(GL11.GL_LINE_STRIP);
                tess.addVertex(lastPoint.xCoord + 0.5D, lastPoint.yCoord, lastPoint.zCoord + 0.5D);
                tess.addVertex((lastPoint.xCoord + pathPoint.xCoord) / 2D + 0.5D, Math.max(lastPoint.yCoord, pathPoint.yCoord), (lastPoint.zCoord + pathPoint.zCoord) / 2D + 0.5D);
                tess.addVertex(pathPoint.xCoord + 0.5D, pathPoint.yCoord, pathPoint.zCoord + 0.5D);
                tess.draw();
            }
        } else {
            if(noDestinationPath) {
                if(increaseAlpha) {
                    alphaValue += 0.005D;
                    if(alphaValue > 0.3D) increaseAlpha = false;
                } else {
                    alphaValue -= 0.005D;
                    if(alphaValue < 0.2D) increaseAlpha = true;
                }
            } else {
                if(alphaValue > 0.2D) alphaValue -= 0.005D;
            }
            for(int i = 0; i < path.getCurrentPathLength(); i++) {
                double red = 1;
                if(path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005D;
                }
                GL11.glColor4d(red, 1 - red, 0, alphaValue);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                tess.startDrawingQuads();
                tess.addVertex(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord);
                tess.addVertex(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord + 1);
                tess.addVertex(pathPoint.xCoord + 1, pathPoint.yCoord, pathPoint.zCoord + 1);
                tess.addVertex(pathPoint.xCoord + 1, pathPoint.yCoord, pathPoint.zCoord);
                tess.draw();
            }
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public boolean tracedToDestination(){
        if(path == null) return false;
        PathPoint finalPoint = path.getFinalPathPoint();
        return finalPoint.xCoord == targetX && finalPoint.yCoord == targetY && finalPoint.zCoord == targetZ;
    }
}
