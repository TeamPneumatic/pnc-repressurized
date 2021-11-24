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
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class RenderPressureChamberInterface extends AbstractTileModelRenderer<TileEntityPressureChamberInterface> {
    private final ModelRenderer input;
    private final ModelRenderer output;

    public RenderPressureChamberInterface(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        input = new ModelRenderer(128, 128, 0, 84);
        input.addBox(0F, 0F, 0F, 10, 10, 2);
        input.setPos(-5F, 11F, -7.2F);
        input.mirror = true;
        output = new ModelRenderer(128, 128, 24, 84);
        output.addBox(0F, 0F, 0F, 10, 10, 2);
        output.setPos(-5F, 11F, 5.2F);
        output.mirror = true;
    }

    @Override
    public void renderModel(TileEntityPressureChamberInterface te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PRESSURE_CHAMBER_INTERFACE));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        float inputProgress = MathHelper.lerp(partialTicks, te.oldInputProgress, te.inputProgress) / MAX_PROGRESS;
        float outputProgress = MathHelper.lerp(partialTicks, te.oldOutputProgress, te.outputProgress) / MAX_PROGRESS;
        if (inputProgress < 1f) {
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(inputProgress * Math.PI)) * 0.37F, 0, 0);
            matrixStackIn.scale(1F - inputProgress, 1, 1);
            input.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
        if (outputProgress < 1f) {
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(outputProgress * Math.PI)) * 0.37F, 0, 0);
            matrixStackIn.scale(1F - outputProgress, 1, 1);
            output.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
    }

    @Override
    protected void renderExtras(TileEntityPressureChamberInterface te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getStackInInterface().isEmpty()) {
            matrixStack.pushPose();

            matrixStack.translate(0.5, 0.5, 0.5);
            RenderUtils.rotateMatrixForDirection(matrixStack, te.getRotation());
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            IBakedModel ibakedmodel = itemRenderer.getModel(te.getStackInInterface(), te.getLevel(), null);
            itemRenderer.render(te.getStackInInterface(), ItemCameraTransforms.TransformType.FIXED, true, matrixStack, buffer, combinedLightIn, combinedOverlayIn, ibakedmodel);

            matrixStack.popPose();
        }
    }
}
