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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class DroneHeldItemLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    DroneHeldItemLayer(RenderLayerParent<DroneRenderState, ModelDrone> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        if (state.heldItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        // note: transform is currently set up so items render upside down
        poseStack.translate(0.0D, 1.5D, 0.0D);
        if (state.heldItemUpsideDown) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        poseStack.scale(state.heldItemScale, state.heldItemScale, state.heldItemScale);
        state.heldItem.submit(poseStack, submitNodeCollector, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
        poseStack.popPose();
    }
}
