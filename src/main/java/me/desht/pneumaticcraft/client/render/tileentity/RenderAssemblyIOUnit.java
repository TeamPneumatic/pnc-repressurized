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
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.tuple.Pair;

public class RenderAssemblyIOUnit extends AbstractTileModelRenderer<TileEntityAssemblyIOUnit> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase;
    private final ModelRenderer armMiddle;
    private final ModelRenderer clawBase;
    private final ModelRenderer clawAxle;
    private final ModelRenderer clawTurn;
    private final ModelRenderer claw1;
    private final ModelRenderer claw2;

    private static final float ITEM_SCALE = 0.5F;

    public RenderAssemblyIOUnit(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 64, 0, 0);
        baseTurn.setPos(-3.5F, 22.0F, -3.5F);
        baseTurn.texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 9.0F, 1.0F, 9.0F, 0.0F, true);

        baseTurn2 = new ModelRenderer(64, 64, 0, 0);
        baseTurn2.setPos(-2.0F, 17.0F, -2.0F);
        baseTurn2.texOffs(0, 30).addBox(-2.0F, -0.5F, 0.5F, 2.0F, 6.0F, 3.0F, 0.2F, false);
        baseTurn2.texOffs(0, 10).addBox(-2.0F, 3.75F, -2.0F, 2.0F, 2.0F, 8.0F, 0.0F, true);
        baseTurn2.texOffs(10, 30).addBox(4.0F, -0.5F, 0.5F, 2.0F, 6.0F, 3.0F, 0.2F, true);
        baseTurn2.texOffs(0, 20).addBox(4.0F, 3.75F, -2.0F, 2.0F, 2.0F, 8.0F, 0.0F, true);

        armBase = new ModelRenderer(64, 64, 0, 0);
        armBase.setPos(-3.0F, 17.0F, -1.0F);
        armBase.texOffs(0, 49).addBox(2.0F, 0.0F, 1.0F, 2.0F, 2.0F, 5.0F, 0.3F, true);
        armBase.texOffs(0, 43).addBox(1.5F, -0.5F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, true);
        armBase.texOffs(12, 43).addBox(1.5F, -0.5F, 5.5F, 3.0F, 3.0F, 3.0F, 0.0F, true);
        armBase.texOffs(0, 39).addBox(-1.5F, 0.0F, 0.0F, 9.0F, 2.0F, 2.0F, 0.0F, true);

        armMiddle = new ModelRenderer(64, 64, 0, 0);
        armMiddle.setPos(-4.0F, 2.0F, 5.0F);
        armMiddle.texOffs(20, 10).addBox(0.0F, 2.0F, 0.0F, 2.0F, 13.0F, 2.0F, 0.0F, true);
        armMiddle.texOffs(12, 24).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 0.3F, true);
        armMiddle.texOffs(0, 24).addBox(0.0F, 15.0F, 0.0F, 2.0F, 2.0F, 2.0F, 0.3F, true);
        armMiddle.texOffs(14, 52).addBox(-0.5F, 15.0F, 0.0F, 3.0F, 2.0F, 2.0F, 0.0F, true);


        clawBase = new ModelRenderer(64, 64, 0, 0);
        clawBase.setPos(-1.0F, 2.0F, 4.5F);
        clawBase.texOffs(46, 0).addBox(-1.0F, -1.0F, 0.0F, 4.0F, 4.0F, 5.0F, 0.0F, true);

        clawAxle = new ModelRenderer(64, 64, 0, 0);
        clawAxle.setPos(-0.5F, 2.5F, 4.0F);
        clawAxle.texOffs(58, 9).addBox(-0.5F, -0.5F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        clawTurn = new ModelRenderer(64, 64, 0, 0);
        clawTurn.setPos(-2.0F, 2.0F, 3.0F);
        clawTurn.texOffs(54, 12).addBox(0.0F, -0.5F, 0.0F, 4.0F, 3.0F, 1.0F, 0.1F, true);

        claw1 = new ModelRenderer(64, 64, 0, 0);
        claw1.setPos(0.0F, 2.0F, 2.25F);
        claw1.texOffs(52, 21).addBox(-0.1F, -0.5F, -1.35F, 1.0F, 3.0F, 2.0F, -0.1F, true);
        claw1.texOffs(58, 21).addBox(0.25F, 0.0F, -1.35F, 1.0F, 2.0F, 2.0F, 0.0F, true);

        claw2 = new ModelRenderer(64, 64, 0, 0);
        claw2.setPos(-1.0F, 2.0F, 2.25F);
        claw2.texOffs(52, 16).addBox(0.1F, -0.5F, -1.35F, 1.0F, 3.0F, 2.0F, -0.1F, true);
        claw2.texOffs(58, 16).addBox(-0.25F, 0.0F, -1.35F, 1.0F, 2.0F, 2.0F, 0.0F, true);
    }

    @Override
    void renderModel(TileEntityAssemblyIOUnit te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 5; i++) {
            angles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
        }

        ItemStack heldStack = te.getPrimaryInventory().getStackInSlot(0);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(getTexture(te)));
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(MathHelper.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angles[0]));

        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);

        armBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);

        armMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);

        clawBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 0);
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(angles[4]));
        matrixStackIn.translate(0, -3 / 16F, 0);

        clawAxle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        clawTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.pushPose();
        matrixStackIn.translate(clawTranslation.getRight(), 0, 0);

        claw1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(-2 * clawTranslation.getRight(), 0, 0);

        claw2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();

        if (!heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = clawTranslation.getLeft();
            if (renderOverride == null || renderOverride.applyRenderChangeIOUnit(matrixStackIn, heldStack)) {
                matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90));
                double yOffset = heldStack.getItem() instanceof BlockItem ? 1.5 / 16D : 0.5 / 16D;
                matrixStackIn.translate(0, yOffset, -3 / 16D);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStackIn.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getModel(heldStack, te.getLevel(), null);
                itemRenderer.render(heldStack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);
            }
        }
    }

    private Pair<IAssemblyRenderOverriding, Float> getClawTranslation(float clawProgress, ItemStack heldStack) {
        float clawTrans;
        IAssemblyRenderOverriding renderOverride = null;
        if (!heldStack.isEmpty()) {
            renderOverride = GuiRegistry.renderOverrides.get(heldStack.getItem().getRegistryName());
            if (renderOverride != null) {
                clawTrans = renderOverride.getIOUnitClawShift(heldStack);
            } else {
                if (heldStack.getItem() instanceof BlockItem) {
                    clawTrans = 1.5F / 16F - clawProgress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - clawProgress * 1.4F / 16F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - clawProgress * 1.5F / 16F;
        }
        return Pair.of(renderOverride, clawTrans);
    }

    private ResourceLocation getTexture(TileEntityAssemblyIOUnit te) {
        return te != null && te.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT;
    }
}
