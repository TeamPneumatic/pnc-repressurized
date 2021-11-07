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
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class RenderAirCannon extends AbstractTileModelRenderer<TileEntityAirCannon> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseFrame1;
    private final ModelRenderer baseFrame2;
    private final ModelRenderer axis;
    private final ModelRenderer cannon;

    public RenderAirCannon(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 64, 0, 0);
        baseTurn.setPos(-3.5F, 20.0F, -5.0F);
        baseTurn.texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 7.0F, 1.0F, 7.0F, 0.0F, false);

        baseFrame1 = new ModelRenderer(64, 64, 0, 0);
        baseFrame1.setPos(-3.5F, 15.0F, -3.0F);
        baseFrame1.texOffs(28, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 3.0F, 0.0F, true);

        baseFrame2 = new ModelRenderer(64, 64, 0, 0);
        baseFrame2.setPos(2.5F, 15.0F, -3.0F);
        baseFrame2.texOffs(36, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 3.0F, 0.0F, true);

        axis = new ModelRenderer(64, 64, 0, 0);
        axis.setPos(-3.0F, 15.5F, -2.0F);
        axis.texOffs(44, 4).addBox(-1.0F, 0.0F, -0.5F, 8.0F, 2.0F, 2.0F, -0.2F, true);

        cannon = new ModelRenderer(64, 64, 0, 0);
        cannon.setPos(-1.0F, 15.0F, -2.5F);
        cannon.texOffs(0, 8).addBox(-1.0F, 0.0F, -1.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);
        cannon.texOffs(24, 8).addBox(-0.5F, -2.0F, -0.5F, 3.0F, 2.0F, 3.0F, 0.0F, true);
        cannon.texOffs(36, 8).addBox(-1.0F, -3.75F, -0.5F, 1.0F, 2.0F, 3.0F, -0.2F, true);
        cannon.texOffs(44, 8).addBox(2.0F, -3.75F, -0.5F, 1.0F, 2.0F, 3.0F, -0.2F, true);
        cannon.texOffs(44, 13).addBox(-1.0F, -3.75F, -1.0F, 4.0F, 2.0F, 1.0F, -0.2F, true);
        cannon.texOffs(34, 13).addBox(-1.0F, -3.75F, 2.0F, 4.0F, 2.0F, 1.0F, -0.2F, true);
    }

    @Override
    void renderModel(TileEntityAirCannon te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_AIR_CANNON));

        float angle = RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        float rotationAngle = te.rotationAngle - angle + 180F;

        matrixStackIn.translate(0.0, 0.0, -0.09375D);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));
        matrixStackIn.translate(0.0, 0.0, 0.09375D);
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        axis.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0.0D, 1.0D, -0.09375D);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(te.heightAngle));
        matrixStackIn.translate(0.0D, -1.0D, 0.09375D);
        cannon.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
