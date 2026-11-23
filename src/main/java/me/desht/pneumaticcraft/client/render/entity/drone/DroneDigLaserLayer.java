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

package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public class DroneDigLaserLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    private static final float LASER_SIZE = 0.4f;

    DroneDigLaserLayer(RenderLayerParent<DroneRenderState, ModelDrone> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        if (state.digLaserStart != null && state.digLaserEnd != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(0, -1, 0);
            renderLaser(poseStack, submitNodeCollector, state, state.digLaserStart, state.digLaserEnd);
            poseStack.popPose();
        }
    }

    private void renderLaser(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, DroneRenderState state, Vec3 start, Vec3 end) {
        float laserLength = (float) start.distanceTo(end);

        poseStack.pushPose();

        poseStack.translate(start.x, start.y, start.z);

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;
        float f3 = Mth.sqrt((float) (dx * dx + dz * dz));
        double rotYawRad = Math.atan2(dx, dz);
        double rotPitchRad = Math.PI / 2.0 - Math.atan2(dy, f3);

        poseStack.mulPose(Axis.YP.rotation((float) rotYawRad));
        poseStack.mulPose(Axis.XP.rotation((float) rotPitchRad));

        poseStack.scale(LASER_SIZE, LASER_SIZE, LASER_SIZE);
        poseStack.translate(0, 0.6, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ageInTicks));

        poseStack.pushPose();
        poseStack.scale(1f, laserLength / LASER_SIZE * 2, 1f);

        int[] cols = RenderUtils.decomposeColor(state.laserColor);

        submitQuad(poseStack, submitNodeCollector, Textures.RENDER_LASER, cols);  // glow
        submitQuad(poseStack, submitNodeCollector, Textures.RENDER_LASER_OVERLAY, cols);  // core

        poseStack.popPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        submitQuad(poseStack, submitNodeCollector, Textures.RENDER_LASER_START, cols);  // glow
        submitQuad(poseStack, submitNodeCollector, Textures.RENDER_LASER_START_OVERLAY, cols);  // core

        poseStack.popPose();
    }

    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier texture, int[] cols) {
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.getTextureRenderColored(texture), (pose, builder) ->
                renderQuad(pose.pose(), builder, cols));
    }

    private static void renderQuad(Matrix4f posMat, VertexConsumer builder, int[] cols) {
        builder.addVertex(posMat,-0.5f, 0f, 0f).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(0, 0).setLight(FULL_BRIGHT);
        builder.addVertex(posMat,-0.5f, 1f, 0f).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(0, 1).setLight(FULL_BRIGHT);
        builder.addVertex(posMat, 0.5f, 1f, 0f).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(1, 1).setLight(FULL_BRIGHT);
        builder.addVertex(posMat, 0.5f, 0f, 0f).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(1, 0).setLight(FULL_BRIGHT);
    }
}
