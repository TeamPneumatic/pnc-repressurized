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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class RenderPressureChamber extends TileEntityRenderer<TileEntityPressureChamberValve> {

    public RenderPressureChamber(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityPressureChamberValve te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
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
            double ticks = Minecraft.getInstance().level.getGameTime() + partialTicks;
            float yBob = MathHelper.sin(((float) ticks  / 10) % 360) * 0.01f;
            float yRot = (float) (ticks / 2) % 360;

            for (int i = 0; i < stacks.size(); i++) {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(i * degreesPerStack));
                matrixStackIn.translate(circleRadius, yBob + 0.2, 0);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yRot));
                matrixStackIn.scale(0.5f, 0.5f, 0.5f);

                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getModel(stacks.get(i), te.getLevel(), null);
                itemRenderer.render(stacks.get(i), ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, light, combinedOverlayIn, ibakedmodel);

                matrixStackIn.popPose();
            }
            matrixStackIn.popPose();
        }
    }
}
