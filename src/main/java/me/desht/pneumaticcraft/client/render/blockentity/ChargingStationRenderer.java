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
import me.desht.pneumaticcraft.api.client.IChargingStationRenderOverride;
import me.desht.pneumaticcraft.client.ClientRegistryImpl;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ChargingStationRenderer implements BlockEntityRenderer<ChargingStationBlockEntity> {
    public ChargingStationRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ChargingStationBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack renderStack = te.getChargingStackSynced();
        if (renderStack.isEmpty() || !te.nonNullLevel().isLoaded(te.getBlockPos()) || te.getCamouflage() != null) return;

        matrixStackIn.pushPose();

        IChargingStationRenderOverride override = ClientRegistryImpl.getInstance().getChargingRenderOverride(renderStack.getItem());

        if (override.onRender(matrixStackIn, renderStack, partialTicks, bufferIn, combinedLightIn, combinedOverlayIn)) {
            matrixStackIn.translate(0.5, 0.5, 0.5);
            RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
            if (!(renderStack.getItem() instanceof BlockItem)) {
                matrixStackIn.scale(0.5F, 0.5F, 0.5F);
            }

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedModel = itemRenderer.getModel(renderStack, te.getLevel(), null, 0);
            itemRenderer.render(renderStack, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, bakedModel);
        }

        matrixStackIn.popPose();
    }
}
