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
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.DyeColor;

public class DroneColourLayer extends RenderLayer<AbstractDroneEntity, ModelDrone> {
    private final ModelDroneCore model;

    DroneColourLayer(RenderLayerParent<AbstractDroneEntity, ModelDrone> rendererIn) {
        super(rendererIn);

        model = new ModelDroneCore(Minecraft.getInstance().getEntityModels().bakeLayer(PNCModelLayers.DRONE_CORE));
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractDroneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        int col = 0xFF000000 | DyeColor.byId(entityIn.getDroneColor()).getTextureDiffuseColor();
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(Textures.DRONE_ENTITY));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), col);
    }
}
