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
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.math.vector.Vector3f;

public class DroneTargetLaserLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    DroneTargetLaserLayer(RenderDrone rendererer) {
        super(rendererer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entitylivingbaseIn instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) entitylivingbaseIn;
            ProgressingLine targetLine = drone.getTargetLine();
            ProgressingLine oldTargetLine = drone.getOldTargetLine();
            if (targetLine != null && oldTargetLine != null) {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
                matrixStackIn.translate(0, -1.5, 0);
                matrixStackIn.scale(2f, 2f, 2f);
                IVertexBuilder builder = bufferIn.getBuffer(RenderType.LINES);
                RenderUtils.renderProgressingLine(oldTargetLine, targetLine, partialTicks, matrixStackIn, builder, 0xFFFF0000);
                matrixStackIn.popPose();
            }
        }
    }
}
