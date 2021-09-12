package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackClientHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderNavigator {
    private final BlockPos targetPos;
    private Path path;
    private boolean increaseAlpha;
    private float alphaValue = 0.2F;

    public RenderNavigator(BlockPos targetPos) {
        this.targetPos = targetPos;
        updatePath();
    }

    public void updatePath() {
        PlayerEntity player = Minecraft.getInstance().player;
        MobEntity e = PneumaticCraftUtils.createDummyEntity(player);
        e.setOnGround(player.isOnGround());
        path = e.getNavigation().createPath(targetPos, 1);
        // TODO: this just doesn't work anymore
        if (!tracedToDestination()) {
            path = CoordTrackClientHandler.getDronePath(player, targetPos);
        }
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, boolean wirePath, boolean xRayEnabled, float partialTicks) {
        if (path == null) return;

        boolean hasDestinationPath = tracedToDestination();

        matrixStack.pushPose();
        matrixStack.translate(0, 0.01D, 0);

        Matrix4f posMat = matrixStack.last().pose();
        if (wirePath) {
            // Draws just wires
            // TODO line stippling
//            if (!hasDestinationPath) {
//                GL11.glEnable(GL11.GL_LINE_STIPPLE);
//                GL11.glLineStipple(2, (short) 0x00FF);
//            }
            IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getNavPath(xRayEnabled, false));
            for (int i = 1; i < path.getNodeCount(); i++) {
                float red = 1;
                if (path.getNodeCount() - i < 200) {
                    red = (path.getNodeCount() - i) * 0.005F;
                }
                PathPoint lastPoint = path.getNode(i - 1);
                PathPoint pathPoint = path.getNode(i);
                builder.vertex(posMat, lastPoint.x + 0.5F, lastPoint.y, lastPoint.z + 0.5F).color(red, 1 - red, 0, 0.5f).endVertex();
                builder.vertex(posMat, (lastPoint.x + pathPoint.x) / 2F + 0.5F, Math.max(lastPoint.y, pathPoint.y), (lastPoint.z + pathPoint.z) / 2F + 0.5F).color(red, 1 - red, 0, 0.5f).endVertex();
                builder.vertex(posMat, pathPoint.x + 0.5F, pathPoint.y, pathPoint.z + 0.5F).color(red, 1 - red, 0, 0.5f).endVertex();
            }
        } else {
            IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getNavPath(xRayEnabled, true));
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
            for (int i = 0; i < path.getNodeCount(); i++) {
                float red = 1;
                if (path.getNodeCount() - i < 200) {
                    red = (path.getNodeCount() - i) * 0.005F;
                }
                PathPoint pathPoint = path.getNode(i);
                builder.vertex(posMat, pathPoint.x, pathPoint.y, pathPoint.z).color(red, 1 - red, 0, alphaValue).endVertex();
                builder.vertex(posMat, pathPoint.x, pathPoint.y, pathPoint.z + 1).color(red, 1 - red, 0, alphaValue).endVertex();
                builder.vertex(posMat, pathPoint.x + 1, pathPoint.y, pathPoint.z + 1).color(red, 1 - red, 0, alphaValue).endVertex();
                builder.vertex(posMat, pathPoint.x + 1, pathPoint.y, pathPoint.z).color(red, 1 - red, 0, alphaValue).endVertex();
            }
        }

        matrixStack.popPose();
    }

    public boolean tracedToDestination() {
        if (path == null) return false;
        PathPoint finalPoint = path.getEndNode();
        return finalPoint != null && targetPos.equals(new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z));
    }
}
