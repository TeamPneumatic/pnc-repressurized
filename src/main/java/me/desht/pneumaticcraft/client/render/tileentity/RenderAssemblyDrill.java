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
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyDrill;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderAssemblyDrill extends AbstractTileModelRenderer<TileEntityAssemblyDrill> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase1;
    private final ModelRenderer armBase2;
    private final ModelRenderer supportMiddle;
    private final ModelRenderer armMiddle1;
    private final ModelRenderer armMiddle2;
    private final ModelRenderer drillBase;
    private final ModelRenderer drill;

    public RenderAssemblyDrill(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 64, 0, 17);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setPos(-3.5F, 22F, -3.5F);
        baseTurn.mirror = true;
        baseTurn2 = new ModelRenderer(64, 64, 28, 17);
        baseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        baseTurn2.setPos(-2F, 17F, -2F);
        baseTurn2.mirror = true;

        armBase1 = new ModelRenderer(64, 64, 0, 25);
        armBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase1.setPos(2F, 17F, -1F);
        armBase1.mirror = true;
        armBase2 = new ModelRenderer(64, 64, 0, 25);
        armBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase2.setPos(-3F, 17F, -1F);
        armBase2.mirror = true;

        supportMiddle = new ModelRenderer(64, 64, 0, 57);
        supportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        supportMiddle.setPos(-1F, 17.5F, 5.5F);
        supportMiddle.mirror = true;

        armMiddle1 = new ModelRenderer(64, 64, 0, 35);
        armMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle1.setPos(-2F, 2F, 5F);
        armMiddle1.mirror = true;
        armMiddle2 = new ModelRenderer(64, 64, 0, 35);
        armMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle2.setPos(1F, 2F, 5F);
        armMiddle2.mirror = true;

        drillBase = new ModelRenderer(64, 64, 8, 38);
        drillBase.addBox(0F, 0F, 0F, 2, 2, 3);
        drillBase.setPos(-1F, 2F, 4.5F);
        drillBase.mirror = true;

        drill = new ModelRenderer(64, 64, 23, 54);
        drill.addBox(0F, 0F, 0F, 1, 1, 4);
        drill.setPos(-0.5F, 2.5F, 1F);
        drill.mirror = true;
    }

    @Override
    void renderModel(TileEntityAssemblyDrill te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 4; i++) {
            angles[i] = MathHelper.lerp(partialTicks, te.oldAngles[i], te.angles[i]);
        }
        angles[4] = MathHelper.lerp(partialTicks, te.oldDrillRotation, te.drillRotation);

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_LASER_AND_DRILL));

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angles[0]));
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);
        armBase1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armBase2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        supportMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);
        armMiddle1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armMiddle2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);
        drillBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 3 / 16F, 0);
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(angles[4]));
        matrixStackIn.translate(0, -3 / 16F, 0);
        drill.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
