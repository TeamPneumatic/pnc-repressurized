/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.CoordTrackClientHandler;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.joml.Matrix4f;

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
        Player player = Minecraft.getInstance().player;
        Mob e = PneumaticCraftUtils.createDummyEntity(player);
        e.setOnGround(player.onGround());
        path = e.getNavigation().createPath(targetPos, 1);
        // TODO: this just doesn't work anymore
        if (!tracedToDestination()) {
            path = CoordTrackClientHandler.getDronePath(player, targetPos);
        }
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, boolean wirePath, boolean xRayEnabled, float partialTicks) {
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
//            VertexConsumer builder = buffer.getBuffer(ModRenderTypes.getNavPath(xRayEnabled, false));
            VertexConsumer builder = buffer.getBuffer(ModRenderTypes.getLineLoops(5.0));
            for (int i = 1; i < path.getNodeCount(); i++) {
                float red = 1;
                if (path.getNodeCount() - i < 200) {
                    red = (path.getNodeCount() - i) * 0.005F;
                }
                Node lastPoint = path.getNode(i - 1);
                Node pathPoint = path.getNode(i);
                builder.vertex(posMat, lastPoint.x + 0.5F, lastPoint.y, lastPoint.z + 0.5F)
                        .color(red, 1 - red, 0, 0.5f)
                        .normal(matrixStack.last(), pathPoint.x - lastPoint.x, pathPoint.y - lastPoint.y, pathPoint.z - lastPoint.z)
                        .endVertex();
                builder.vertex(posMat, (lastPoint.x + pathPoint.x) / 2F + 0.5F, Math.max(lastPoint.y, pathPoint.y), (lastPoint.z + pathPoint.z) / 2F + 0.5F)
                        .color(red, 1 - red, 0, 0.5f)
                        .normal(matrixStack.last(), pathPoint.x - lastPoint.x, pathPoint.y - lastPoint.y, pathPoint.z - lastPoint.z)
                        .endVertex();
                builder.vertex(posMat, pathPoint.x + 0.5F, pathPoint.y, pathPoint.z + 0.5F)
                        .color(red, 1 - red, 0, 0.5f)
                        .normal(matrixStack.last(), pathPoint.x - lastPoint.x, pathPoint.y - lastPoint.y, pathPoint.z - lastPoint.z)
                        .endVertex();
            }
        } else {
            VertexConsumer builder = buffer.getBuffer(xRayEnabled ? ModRenderTypes.UNTEXTURED_QUAD_NO_DEPTH : ModRenderTypes.UNTEXTURED_QUAD);
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
                Node pathPoint = path.getNode(i);
                builder.vertex(posMat, pathPoint.x, pathPoint.y, pathPoint.z).color(red, 1 - red, 0, alphaValue)
                        .uv2(RenderUtils.FULL_BRIGHT)
                        .endVertex();
                builder.vertex(posMat, pathPoint.x, pathPoint.y, pathPoint.z + 1).color(red, 1 - red, 0, alphaValue)
                        .uv2(RenderUtils.FULL_BRIGHT)
                        .endVertex();
                builder.vertex(posMat, pathPoint.x + 1, pathPoint.y, pathPoint.z + 1).color(red, 1 - red, 0, alphaValue)
                        .uv2(RenderUtils.FULL_BRIGHT)
                        .endVertex();
                builder.vertex(posMat, pathPoint.x + 1, pathPoint.y, pathPoint.z).color(red, 1 - red, 0, alphaValue)
                        .uv2(RenderUtils.FULL_BRIGHT)
                        .endVertex();
            }
        }

        matrixStack.popPose();
    }

    public boolean tracedToDestination() {
        if (path == null) return false;
        Node finalPoint = path.getEndNode();
        return finalPoint != null && targetPos.equals(new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z));
    }
}
