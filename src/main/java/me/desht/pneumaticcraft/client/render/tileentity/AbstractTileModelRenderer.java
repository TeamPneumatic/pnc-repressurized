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
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public abstract class AbstractTileModelRenderer<T extends AbstractPneumaticCraftBlockEntity> implements BlockEntityRenderer<T> {
    AbstractTileModelRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(T te, float partialTicks, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        // boilerplate translation code, common to all our model-rendering TERs, is done here

        if (!shouldRender(te) || !te.nonNullLevel().isLoaded(te.getBlockPos())) return;

        matrixStack.pushPose();

        // necessary transforms to make models render in the right place
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);

        // actual model rendering work
        renderModel(te, partialTicks, matrixStack, iRenderTypeBuffer, combinedLightIn, combinedOverlayIn);

        matrixStack.popPose();

        renderExtras(te, partialTicks, matrixStack, iRenderTypeBuffer, combinedLightIn, combinedOverlayIn);
    }

    abstract void renderModel(T te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn);

    protected boolean shouldRender(T te) {
        return true;
    }

    protected void renderExtras(T te, float partialTicks, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
    }

}
