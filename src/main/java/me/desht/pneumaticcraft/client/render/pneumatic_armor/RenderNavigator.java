package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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
    private Path path;
    private boolean increaseAlpha;
    private float alphaValue = 0.2F;

    public RenderNavigator(World world, BlockPos targetPos) {
        this.targetPos = targetPos;
        updatePath();
    }

    public void updatePath() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        path = PneumaticCraftUtils.getPathFinder().findPath(player.world, PneumaticCraftUtils.createDummyEntity(player), targetPos, CoordTrackUpgradeHandler.SEARCH_RANGE);
        // TODO: this just doesn't work anymore
        if (!tracedToDestination()) {
            path = CoordTrackUpgradeHandler.getDronePath(player, targetPos);
        }
    }

    public void render(boolean wirePath, boolean xRayEnabled, float partialTicks) {
        if (path == null) return;

        GlStateManager.depthMask(false);
        if (xRayEnabled) GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(5.0F);

        boolean hasDestinationPath = tracedToDestination();

        BufferBuilder wr = Tessellator.getInstance().getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.01D, 0);

        // Draws just wires
        if (wirePath) {
            if (!hasDestinationPath) {
                GL11.glEnable(GL11.GL_LINE_STIPPLE);
                GL11.glLineStipple(2, (short) 0x00FF);
            }
            for (int i = 1; i < path.getCurrentPathLength(); i++) {
                float red = 1;
                if (path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005F;
                }
                GlStateManager.color(red, 1 - red, 0, 0.5F);
                PathPoint lastPoint = path.getPathPointFromIndex(i - 1);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                wr.pos(lastPoint.x + 0.5D, lastPoint.y, lastPoint.z + 0.5D).endVertex();
                wr.pos((lastPoint.x + pathPoint.x) / 2D + 0.5D, Math.max(lastPoint.y, pathPoint.y), (lastPoint.z + pathPoint.z) / 2D + 0.5D).endVertex();
                wr.pos(pathPoint.x + 0.5D, pathPoint.y, pathPoint.z + 0.5D).endVertex();
                Tessellator.getInstance().draw();
            }
        } else {
            if (hasDestinationPath) {
                if (alphaValue > 0.2F) alphaValue -= 0.005F;
            } else {
                if (increaseAlpha) {
                    alphaValue += 0.005F;
                    if (alphaValue > 0.3F) increaseAlpha = false;
                } else {
                    alphaValue -= 0.005F;
                    if (alphaValue < 0.2F) increaseAlpha = true;
                }
            }
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                float red = 1;
                if (path.getCurrentPathLength() - i < 200) {
                    red = (path.getCurrentPathLength() - i) * 0.005F;
                }
                GlStateManager.color(red, 1 - red, 0, alphaValue);
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                wr.pos(pathPoint.x, pathPoint.y, pathPoint.z).endVertex();
                wr.pos(pathPoint.x, pathPoint.y, pathPoint.z + 1).endVertex();
                wr.pos(pathPoint.x + 1, pathPoint.y, pathPoint.z + 1).endVertex();
                wr.pos(pathPoint.x + 1, pathPoint.y, pathPoint.z).endVertex();
                Tessellator.getInstance().draw();
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
    }

    public boolean tracedToDestination() {
        if (path == null) return false;
        PathPoint finalPoint = path.getFinalPathPoint();
        return finalPoint != null && targetPos.equals(new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z));
    }
}
