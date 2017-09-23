package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderNavigator {
    private final BlockPos targetPos;
    private final World world;
    private Path path;
    private boolean increaseAlpha;
    private double alphaValue = 0.2D;

    public RenderNavigator(World world, BlockPos targetPos) {
        this.targetPos = targetPos;
        this.world = world;
        updatePath();
    }

    public void updatePath() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        path = PneumaticCraftUtils.getPathFinder().findPath(player.world, PneumaticCraftUtils.createDummyEntity(player), targetPos, CoordTrackUpgradeHandler.SEARCH_RANGE);
        if (!tracedToDestination()) {
            path = CoordTrackUpgradeHandler.getDronePath(player, targetPos);
        }
    }

    public void render(boolean wirePath, boolean xRayEnabled, float partialTicks) {
        if (path == null) return;

        GL11.glDepthMask(false);
        if (xRayEnabled) GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(5.0F);

        boolean noDestinationPath = !tracedToDestination();

        BufferBuilder wr = Tessellator.getInstance().getBuffer();

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0.01D, 0);

        //Draws just wires
        if (wirePath) {
            if (noDestinationPath) {
                GL11.glEnable(GL11.GL_LINE_STIPPLE);
                GL11.glLineStipple(4, (short) 0x00FF);
            }
            for (int i = 1; i < path.getCurrentPathLength(); i++) {
                double red = 1;
                if (path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005D;
                }
                GL11.glColor4d(red, 1 - red, 0, 0.5D);
                PathPoint lastPoint = path.getPathPointFromIndex(i - 1);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                wr.pos(lastPoint.x + 0.5D, lastPoint.y, lastPoint.z + 0.5D).endVertex();
                wr.pos((lastPoint.x + pathPoint.x) / 2D + 0.5D, Math.max(lastPoint.y, pathPoint.y), (lastPoint.z + pathPoint.z) / 2D + 0.5D).endVertex();
                wr.pos(pathPoint.x + 0.5D, pathPoint.y, pathPoint.z + 0.5D).endVertex();
                Tessellator.getInstance().draw();
            }
        } else {
            if (noDestinationPath) {
                if (increaseAlpha) {
                    alphaValue += 0.005D;
                    if (alphaValue > 0.3D) increaseAlpha = false;
                } else {
                    alphaValue -= 0.005D;
                    if (alphaValue < 0.2D) increaseAlpha = true;
                }
            } else {
                if (alphaValue > 0.2D) alphaValue -= 0.005D;
            }
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                double red = 1;
                if (path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005D;
                }
                GL11.glColor4d(red, 1 - red, 0, alphaValue);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                wr.pos(pathPoint.x, pathPoint.y, pathPoint.z).endVertex();
                wr.pos(pathPoint.x, pathPoint.y, pathPoint.z + 1).endVertex();
                wr.pos(pathPoint.x + 1, pathPoint.y, pathPoint.z + 1).endVertex();
                wr.pos(pathPoint.x + 1, pathPoint.y, pathPoint.z).endVertex();
                Tessellator.getInstance().draw();
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

    public boolean tracedToDestination() {
        if (path == null) return false;
        PathPoint finalPoint = path.getFinalPathPoint();
        return targetPos.equals(new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z));
    }
}
