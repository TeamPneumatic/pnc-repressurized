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

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.utility.SentryTurretBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class SentryTurretRenderer extends AbstractBlockEntityModelRenderer<SentryTurretBlockEntity> {
    private final ModelMinigun model;

    public SentryTurretRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelMinigun(ctx.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    void renderModel(SentryTurretBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.translate(0, -13 / 16F, 0);
        model.renderMinigun(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.getMinigun(), partialTicks, false);
    }

    @Override
    public AABB getRenderBoundingBox(SentryTurretBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1);
    }
}
