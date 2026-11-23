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
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class DroneTargetLaserLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    DroneTargetLaserLayer(RenderLayerParent<DroneRenderState, ModelDrone> rendererer) {
        super(rendererer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        float[] line = state.targetLine;
        if (line != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(0, -1.5, 0);
            poseStack.scale(2f, 2f, 2f);
            float lineWidth = RenderUtils.defaultLineWidth();
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, builder) ->
                    RenderUtils.normalLine(builder, pose, line[0], line[1], line[2], line[3], line[4], line[5], 1f, 1f, 0f, 0f, lineWidth));
            poseStack.popPose();
        }
    }
}
