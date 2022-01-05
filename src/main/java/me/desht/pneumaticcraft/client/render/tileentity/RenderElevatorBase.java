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
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderElevatorBase extends AbstractTileModelRenderer<TileEntityElevatorBase> {
    private static final float FACTOR = 9F / 16;
    private static final float[] SHADE = new float[] { 1f, 0.85f, 0.7f, 0.55f };

    private final ModelRenderer pole1;
    private final ModelRenderer pole2;
    private final ModelRenderer pole3;
    private final ModelRenderer pole4;
    private final ModelRenderer floor;

    public RenderElevatorBase(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        pole1 = new ModelRenderer(64, 64, 28, 41);
        pole1.setPos(17.0F, 9.0F, -1.0F);
        pole1.addBox(-19.5F, 0.0F, -1.5F, 5.0F, 14.0F, 5.0F, 0.0F, true);

        pole2 = new ModelRenderer(64, 64, 32, 19);
        pole2.setPos(12.0F, 9.0F, -2.0F);
        pole2.addBox(-15.0F, 0.0F, -1.0F, 6.0F, 14.0F, 6.0F, 0.0F, true);

        pole3 = new ModelRenderer(64, 64, 0, 39);
        pole3.setPos(5.0F, 9.0F, -12.0F);
        pole3.addBox(-8.5F, 0.0F, 8.5F, 7.0F, 14.0F, 7.0F, 0.0F, true);

        pole4 = new ModelRenderer(64, 64, 0, 17);
        pole4.setPos(-4.0F, 9.0F, -4.0F);
        pole4.addBox(0.0F, 0.0F, 0.0F, 8.0F, 14.0F, 8.0F, 0.0F, true);

        floor = new ModelRenderer(64, 64, 0, 0);
        floor.setPos(-8.0F, 8.0F, -8.0F);
        floor.addBox(0.0F, 0.0F, 0.0F, 16.0F, 1.0F, 16.0F, 0.0F, false);
    }

    @Override
    public void renderModel(TileEntityElevatorBase te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.extension == 0) return;

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ELEVATOR));

        double extension = MathHelper.lerp(partialTicks, te.oldExtension, te.extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole4, 0, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole3, 1, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole2, 2, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole1, 3, extension);

        floor.render(matrixStackIn, builder, te.lightAbove, combinedOverlayIn);
    }

    @Override
    protected void renderExtras(TileEntityElevatorBase te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.fakeFloorTextureUV != null && te.fakeFloorTextureUV.length == 4) {
            matrixStack.pushPose();
            double extension = MathHelper.lerp(partialTicks, te.oldExtension, te.extension);
            matrixStack.translate(0, extension + 1.0005f, 0);
            IVertexBuilder builder = iRenderTypeBuffer.getBuffer(ModRenderTypes.getTextureRender(AtlasTexture.LOCATION_BLOCKS));
            float uMin = te.fakeFloorTextureUV[0];
            float vMin = te.fakeFloorTextureUV[1];
            float uMax = te.fakeFloorTextureUV[2];
            float vMax = te.fakeFloorTextureUV[3];
            Matrix4f posMat = matrixStack.last().pose();
            builder.vertex(posMat,0, 0, 1).color(1f, 1f, 1f, 1f).uv(uMin, vMax).uv2(te.lightAbove).endVertex();
            builder.vertex(posMat,1, 0, 1).color(1f, 1f, 1f, 1f).uv(uMax, vMax).uv2(te.lightAbove).endVertex();
            builder.vertex(posMat,1, 0, 0).color(1f, 1f, 1f, 1f).uv(uMax, vMin).uv2(te.lightAbove).endVertex();
            builder.vertex(posMat,0, 0, 0).color(1f, 1f, 1f, 1f).uv(uMin, vMin).uv2(te.lightAbove).endVertex();
            matrixStack.popPose();
        }
    }

    private void renderPole(MatrixStack matrixStackIn, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn, ModelRenderer pole, int idx, double extension) {
        matrixStackIn.translate(0, -extension / 4, 0);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, FACTOR, 0);
        matrixStackIn.scale(1, (float) (extension * 16 / 14 / 4), 1);
        matrixStackIn.translate(0, -FACTOR, 0);
        pole.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, SHADE[idx], SHADE[idx], SHADE[idx], 1);
        matrixStackIn.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityElevatorBase te) {
        return true;  // since this can get very tall
    }
}
