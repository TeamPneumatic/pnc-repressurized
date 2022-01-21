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
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;

public class DroneMinigunLayer extends RenderLayer<EntityDroneBase, ModelDrone> {
    private final ModelMinigun modelDroneMinigun;

    DroneMinigunLayer(RenderDrone renderer) {
        super(renderer);

        modelDroneMinigun = new ModelMinigun(Minecraft.getInstance().getEntityModels().bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityIn instanceof EntityDrone drone) {
            if (drone.hasMinigun()) {
                modelDroneMinigun.renderMinigun(matrixStackIn, bufferIn, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), drone.getMinigun(), partialTicks, true);
                renderMinigunTracers(drone, matrixStackIn, bufferIn, partialTicks);
            }
        }
    }

    private void renderMinigunTracers(EntityDrone drone, PoseStack matrixStackIn, MultiBufferSource bufferIn, float partialTicks) {
        if (RenderMinigunTracers.shouldRender(drone.getMinigun())) {
            double x = Mth.lerp(partialTicks, drone.xOld, drone.getX());
            double y = Mth.lerp(partialTicks, drone.yOld, drone.getY());
            double z = Mth.lerp(partialTicks, drone.zOld, drone.getZ());
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStackIn.translate(0, -1.5, 0);
            matrixStackIn.scale(2f, 2f, 2f);
            RenderMinigunTracers.render(drone.getMinigun(), matrixStackIn, bufferIn, x, y, z, 0.6);
            matrixStackIn.popPose();
        }
    }
}
