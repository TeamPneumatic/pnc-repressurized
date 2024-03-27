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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.ClientRegistryImpl;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.AssemblyPlatformBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class AssemblyPlatformRenderer extends AbstractBlockEntityModelRenderer<AssemblyPlatformBlockEntity> {
    private static final float ITEM_SCALE = 0.5F;

    private final ModelPart claw1;
    private final ModelPart claw2;

    private static final String CLAW1 = "claw1";
    private static final String CLAW2 = "claw2";

    public AssemblyPlatformRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ASSEMBLY_PLATFORM);
        claw1 = root.getChild(CLAW1);
        claw2 = root.getChild(CLAW2);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(CLAW1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("claw1_0", -0.5F, 0.0F, 0.1F, 3, 1, 1, new CubeDeformation(-0.1F), 0, 12)
                        .addBox("claw1_1", -0.5F, 0.0F, 0.6F, 3, 1, 1, 8, 14)
                        .mirror(),
                PartPose.offset(-1.0F, 17.0F, 0.0F));
        partdefinition.addOrReplaceChild(CLAW2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("claw2_0", -0.5F, 0.0F, -0.1F, 3, 1, 1, new CubeDeformation(-0.1F), 0, 14)
                        .addBox("claw2_1", -0.5F, 0.0F, -0.6F, 3, 1, 1, 8, 12)
                        .mirror(),
                PartPose.offset(-1.0F, 17.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderModel(AssemblyPlatformBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack heldStack = te.getItemHandler(null).getStackInSlot(0);
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(Mth.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_PLATFORM));

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 0, clawTranslation.getRight());
        claw1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 0, -2 * clawTranslation.getRight());
        claw2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();

        if (!heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = clawTranslation.getLeft();
            if (renderOverride == null || renderOverride.applyRenderChangePlatform(matrixStackIn, heldStack)) {
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(180));
                double yOffset = heldStack.getItem() instanceof BlockItem ? -16.5 / 16F : -17.5 / 16F;
                matrixStackIn.translate(0, yOffset + 0.05, 0);
                matrixStackIn.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                BakedModel bakedModel = itemRenderer.getModel(heldStack, te.getLevel(), null, 0);
                itemRenderer.render(heldStack, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, bakedModel);
            }
        }
    }

    private Pair<IAssemblyRenderOverriding, Float> getClawTranslation(float clawProgress, ItemStack heldStack) {
        float clawTrans;
        IAssemblyRenderOverriding renderOverride = ClientRegistryImpl.getInstance().getAssemblyRenderOverride(heldStack.getItem());
        if (!heldStack.isEmpty()) {
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
