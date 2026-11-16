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
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Unit;

public class DroneColourLayer extends RenderLayer<DroneRenderState, ModelDrone> {
    private final ModelDroneCore model;

    DroneColourLayer(RenderLayerParent<DroneRenderState, ModelDrone> rendererIn, EntityRendererProvider.Context ctx) {
        super(rendererIn);

        model = new ModelDroneCore(ctx.bakeLayer(PNCModelLayers.DRONE_CORE));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DroneRenderState state, float yRot, float xRot) {
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, model.renderType(Textures.DRONE_ENTITY), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.droneColor, null, state.outlineColor, null);
    }
}
