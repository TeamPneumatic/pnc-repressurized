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
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberValveBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PressureChamberRenderer implements BlockEntityRenderer<PressureChamberValveBlockEntity> {

    public PressureChamberRenderer(@SuppressWarnings("unused") BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(PressureChamberValveBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
       if (te.multiBlockSize == 0 || !te.hasGlass) return;

        List<ItemStack> stacks = te.renderedItems;
        if (!stacks.isEmpty()){
            double x = te.multiBlockX - te.getBlockPos().getX() + te.multiBlockSize / 2D;
            double y = te.multiBlockY - te.getBlockPos().getY() + 1.1; // Set to '+ 1' for normal y value.
            double z = te.multiBlockZ - te.getBlockPos().getZ() + te.multiBlockSize / 2D;

            int light = ClientUtils.getLightAt(new BlockPos(te.multiBlockX + te.multiBlockSize / 2, te.multiBlockY + 1, te.multiBlockZ + te.multiBlockSize / 2));
            matrixStackIn.pushPose();
            matrixStackIn.translate(x, y, z);

            // render single item centered (looks best), multiple items arranged in a circle
            // around the centre of the chamber, radius dependent on chamber size
            float circleRadius = stacks.size() == 1 ? 0 : 0.33f * (te.multiBlockSize - 2);
            float degreesPerStack = 360f / stacks.size();

            // some gentle rotation and bobbing looks good here
            double ticks = te.nonNullLevel().getGameTime() + partialTicks;
            float yBob = Mth.sin(((float) ticks  / 10) % 360) * 0.01f;
            float yRot = (float) (ticks / 2) % 360;

            for (int i = 0; i < stacks.size(); i++) {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(i * degreesPerStack));
                matrixStackIn.translate(circleRadius, yBob + 0.2, 0);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(yRot));
                matrixStackIn.scale(0.5f, 0.5f, 0.5f);

                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                BakedModel bakedModel = itemRenderer.getModel(stacks.get(i), te.getLevel(), null, 0);
                itemRenderer.render(stacks.get(i), ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, light, combinedOverlayIn, bakedModel);

                matrixStackIn.popPose();
            }
            matrixStackIn.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(PressureChamberValveBlockEntity blockEntity) {
        return blockEntity.getChamberAABB();
    }
}
