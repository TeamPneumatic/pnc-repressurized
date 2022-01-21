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

package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

public class RenderSentryTurret extends AbstractTileModelRenderer<TileEntitySentryTurret> {
    private final ModelMinigun model;

    public RenderSentryTurret(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelMinigun(ctx.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    void renderModel(TileEntitySentryTurret te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.translate(0, -13 / 16F, 0);
        model.renderMinigun(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.getMinigun(), partialTicks, false);
    }

    @Override
    protected void renderExtras(TileEntitySentryTurret te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (RenderMinigunTracers.shouldRender(te.getMinigun())) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.75, 0.5);
            BlockPos pos = te.getBlockPos();
            RenderMinigunTracers.render(te.getMinigun(), matrixStack, bufferIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            matrixStack.popPose();
        }
    }
}
