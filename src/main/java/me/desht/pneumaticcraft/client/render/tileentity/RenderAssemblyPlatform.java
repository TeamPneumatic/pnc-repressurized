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
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.tuple.Pair;

public class RenderAssemblyPlatform extends AbstractTileModelRenderer<TileEntityAssemblyPlatform> {
    private static final float ITEM_SCALE = 0.5F;

    private final ModelRenderer claw1;
    private final ModelRenderer claw2;

    public RenderAssemblyPlatform(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        claw1 = new ModelRenderer(64, 64, 0, 0);
        claw1.setPos(-1.0F, 17.0F, 0.0F);
        claw1.texOffs(0, 12).addBox(-0.5F, 0.0F, 0.1F, 3.0F, 1.0F, 1.0F, -0.1F, true);
        claw1.texOffs(8, 14).addBox(-0.5F, 0.0F, 0.6F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        claw2 = new ModelRenderer(64, 64, 0, 0);
        claw2.setPos(-1.0F, 17.0F, -1.0F);
        claw2.texOffs(0, 14).addBox(-0.5F, 0.0F, -0.1F, 3.0F, 1.0F, 1.0F, -0.1F, true);
        claw2.texOffs(8, 12).addBox(-0.5F, 0.0F, -0.6F, 3.0F, 1.0F, 1.0F, 0.0F, true);
    }

    @Override
    public void renderModel(TileEntityAssemblyPlatform te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack heldStack = te.getPrimaryInventory().getStackInSlot(0);
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(MathHelper.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_PLATFORM));

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 0, clawTranslation.getRight());
        claw1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 0, -2 * clawTranslation.getRight());
        claw2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();

        if (!heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = clawTranslation.getLeft();
            if (renderOverride == null || renderOverride.applyRenderChangePlatform(matrixStackIn, heldStack)) {
                matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
                double yOffset = heldStack.getItem() instanceof BlockItem ? -16.5 / 16F : -17.5 / 16F;
                matrixStackIn.translate(0, yOffset + 0.05, 0);
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
                clawTrans = renderOverride.getPlatformClawShift(heldStack);
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
}
