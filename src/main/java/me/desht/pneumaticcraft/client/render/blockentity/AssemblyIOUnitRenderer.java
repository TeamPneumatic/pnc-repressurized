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
import me.desht.pneumaticcraft.common.block.entity.AssemblyIOUnitBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class AssemblyIOUnitRenderer extends AbstractBlockEntityModelRenderer<AssemblyIOUnitBlockEntity> {
    private final ModelPart baseTurn;
    private final ModelPart baseTurn2;
    private final ModelPart armBase;
    private final ModelPart armMiddle;
    private final ModelPart clawBase;
    private final ModelPart clawAxle;
    private final ModelPart clawTurn;
    private final ModelPart claw1;
    private final ModelPart claw2;

    private static final float ITEM_SCALE = 0.5F;

    private static final String BASETURN = "baseTurn";
    private static final String BASETURN2 = "baseTurn2";
    private static final String ARMBASE = "armBase";
    private static final String ARMMIDDLE = "armMiddle";
    private static final String CLAWBASE = "clawBase";
    private static final String CLAWAXLE = "clawAxle";
    private static final String CLAWTURN = "clawTurn";
    private static final String CLAW1 = "claw1";
    private static final String CLAW2 = "claw2";

    public AssemblyIOUnitRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ASSEMBLY_IO_UNIT);
        baseTurn = root.getChild(BASETURN);
        baseTurn2 = root.getChild(BASETURN2);
        armBase = root.getChild(ARMBASE);
        armMiddle = root.getChild(ARMMIDDLE);
        clawBase = root.getChild(CLAWBASE);
        clawAxle = root.getChild(CLAWAXLE);
        clawTurn = root.getChild(CLAWTURN);
        claw1 = root.getChild(CLAW1);
        claw2 = root.getChild(CLAW2);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BASETURN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseTurn_0", -1.0F, 0.0F, -1.0F, 9, 1, 9, 0, 0)
                        .mirror(),
                PartPose.offset(-3.5F, 22.0F, -3.5F));
        partdefinition.addOrReplaceChild(BASETURN2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseTurn2_0", -2.0F, -0.5F, 0.5F, 2, 6, 3, new CubeDeformation(0.2F), 0, 30)
                        .addBox("baseTurn2_1", -2.0F, 3.75F, -2.0F, 2, 2, 8, 0, 10)
                        .addBox("baseTurn2_2", 4.0F, -0.5F, 0.5F, 2, 6, 3, new CubeDeformation(0.2F), 10, 30)
                        .addBox("baseTurn2_3", 4.0F, 3.75F, -2.0F, 2, 2, 8, 0, 20)
                        .mirror(),
                PartPose.offset(-2.0F, 17.0F, -2.0F));
        partdefinition.addOrReplaceChild(ARMBASE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("armBase_0", 2.0F, 0.0F, 1.0F, 2, 2, 5, new CubeDeformation(0.3F), 0, 49)
                        .addBox("armBase_1", 1.5F, -0.5F, -0.5F, 3, 3, 3, 0, 43)
                        .addBox("armBase_2", 1.5F, -0.5F, 5.5F, 3, 3, 3, 12, 43)
                        .addBox("armBase_3", -1.5F, 0.0F, 0.0F, 9, 2, 2, 0, 39)
                        .mirror(),
                PartPose.offset(-3.0F, 17.0F, -1.0F));
        partdefinition.addOrReplaceChild(ARMMIDDLE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("armMiddle_0", 0.0F, 2.0F, 0.0F, 2, 13, 2, 20, 10)
                        .addBox("armMiddle_1", 0.0F, 0.0F, 0.0F, 2, 2, 2, new CubeDeformation(0.3F), 12, 24)
                        .addBox("armMiddle_2", 0.0F, 15.0F, 0.0F, 2, 2, 2, new CubeDeformation(0.3F), 0, 24)
                        .addBox("armMiddle_3", -0.5F, 15.0F, 0.0F, 3, 2, 2, 14, 52)
                        .mirror(),
                PartPose.offset(-4.0F, 2.0F, 5.0F));
        partdefinition.addOrReplaceChild(CLAWBASE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("clawBase_0", -1.0F, -1.0F, 0.0F, 4, 4, 5, 46, 0)
                        .mirror(),
                PartPose.offset(-1.0F, 2.0F, 4.5F));
        partdefinition.addOrReplaceChild(CLAWAXLE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("clawAxle_0", -0.5F, -0.5F, 0.0F, 2, 2, 1, 58, 9)
                        .mirror(),
                PartPose.offset(-0.5F, 2.5F, 4.0F));
        partdefinition.addOrReplaceChild(CLAWTURN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("clawTurn_0", 0.0F, -0.5F, 0.0F, 4, 3, 1, new CubeDeformation(0.1F), 54, 12)
                        .mirror(),
                PartPose.offset(-2.0F, 2.0F, 3.0F));
        partdefinition.addOrReplaceChild(CLAW1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("claw1_0", -0.1F, -0.5F, -1.35F, 1, 3, 2, new CubeDeformation(-0.1F), 52, 21)
                        .addBox("claw1_1", 0.25F, 0.0F, -1.35F, 1, 2, 2, 58, 21)
                        .mirror(),
                PartPose.offset(0.0F, 2.0F, 2.25F));
        partdefinition.addOrReplaceChild(CLAW2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("claw2_0", 0.1F, -0.5F, -1.35F, 1, 3, 2, new CubeDeformation(-0.1F), 52, 16)
                        .addBox("claw2_1", -0.25F, 0.0F, -1.35F, 1, 2, 2, 58, 16)
                        .mirror(),
                PartPose.offset(-1.0F, 2.0F, 2.25F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    void renderModel(AssemblyIOUnitBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 5; i++) {
            angles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
        }

        ItemStack heldStack = te.getPrimaryInventory().getStackInSlot(0);
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTexture(te)));
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(Mth.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(angles[0]));

        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);

        armBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);

        armMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);

        clawBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 0);
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(angles[4]));
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
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
                double yOffset = heldStack.getItem() instanceof BlockItem ? 1.5 / 16D : 0.5 / 16D;
                matrixStackIn.translate(0, yOffset, -3 / 16D);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90));
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

    private ResourceLocation getTexture(AssemblyIOUnitBlockEntity te) {
        return te != null && te.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT;
    }
}
