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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTagWorkbench;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;

public class RenderTagWorkbench extends TileEntityRenderer<TileEntityTagWorkbench> {
    public RenderTagWorkbench(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityTagWorkbench te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getLevel().getChunkSource().isEntityTickingChunk(new ChunkPos(te.getBlockPos()))) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1, 0.5);
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                te.displayedStack, 0, 0, -0.25, 0.4f, te.getRotation());
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.paperItemId)), -0.25, 0, 0.25, 0.4f, te.getRotation());
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.outputItemId)), 0.25, 0, 0.25, 0.4f, te.getRotation());
        matrixStackIn.popPose();
    }
}
