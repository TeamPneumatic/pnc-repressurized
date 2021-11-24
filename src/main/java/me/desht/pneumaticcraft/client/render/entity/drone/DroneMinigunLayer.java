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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class DroneMinigunLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    private final ModelMinigun modelDroneMinigun = new ModelMinigun();

    DroneMinigunLayer(RenderDrone renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityIn instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) entityIn;
            if (drone.hasMinigun()) {
                modelDroneMinigun.renderMinigun(matrixStackIn, bufferIn, packedLightIn, LivingRenderer.getOverlayCoords(entityIn, 0.0F), drone.getMinigun(), partialTicks, true);
                renderMinigunTracers(drone, matrixStackIn, bufferIn, partialTicks);
            }
        }
    }

    private void renderMinigunTracers(EntityDrone drone, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks) {
        if (RenderMinigunTracers.shouldRender(drone.getMinigun())) {
            double x = MathHelper.lerp(partialTicks, drone.xOld, drone.getX());
            double y = MathHelper.lerp(partialTicks, drone.yOld, drone.getY());
            double z = MathHelper.lerp(partialTicks, drone.zOld, drone.getZ());
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStackIn.translate(0, -1.5, 0);
            matrixStackIn.scale(2f, 2f, 2f);
            RenderMinigunTracers.render(drone.getMinigun(), matrixStackIn, bufferIn, x, y, z, 0.6);
            matrixStackIn.popPose();
        }
    }
}
