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
import me.desht.pneumaticcraft.common.block.entity.utility.TagWorkbenchBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagWorkbenchRenderer implements BlockEntityRenderer<TagWorkbenchBlockEntity> {
    public TagWorkbenchRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(TagWorkbenchBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.nonNullLevel().isLoaded(te.getBlockPos())) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1, 0.5);
        DisplayTableRenderer.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                te.displayedStack, 0, 0, -0.25, 0.4f, te.getRotation());
        DisplayTableRenderer.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.paperItemId)), -0.25, 0, 0.25, 0.4f, te.getRotation());
        DisplayTableRenderer.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.outputItemId)), 0.25, 0, 0.25, 0.4f, te.getRotation());
        matrixStackIn.popPose();
    }
}
