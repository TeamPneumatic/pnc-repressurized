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
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.ClientRegistryImpl;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.processing.AssemblyIOUnitBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class AssemblyIOUnitRenderer extends AbstractAssemblyRenderer<AssemblyIOUnitBlockEntity, AssemblyIOUnitRenderer.State> {
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
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AssemblyIOUnitBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        ItemStack heldStack = blockEntity.getItemHandler(null).getStackInSlot(0);
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(Mth.lerp(partialTicks, blockEntity.oldClawProgress, blockEntity.clawProgress), heldStack);
        state.texture = getTexture(blockEntity);
        state.heldStack = heldStack.copy();
        state.renderOverride = clawTranslation.getLeft();
        state.clawTranslation = clawTranslation.getRight();
        itemModelResolver.updateForTopItem(state.item, heldStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
    }

    @Override
    void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        float[] angles = state.angles;
        RenderType renderType = RenderTypes.entityCutout(state.texture);

        poseStack.mulPose(Axis.YP.rotationDegrees(angles[0]));

        submitPart(submitNodeCollector, baseTurn, poseStack, renderType, state);
        submitPart(submitNodeCollector, baseTurn2, poseStack, renderType, state);

        poseStack.translate(0, 18 / 16F, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(angles[1]));
        poseStack.translate(0, -18 / 16F, 0);

        submitPart(submitNodeCollector, armBase, poseStack, renderType, state);

        poseStack.translate(0, 18 / 16F, 6 / 16F);
        poseStack.mulPose(Axis.XP.rotationDegrees(angles[2]));
        poseStack.translate(0, -18 / 16F, -6 / 16F);

        submitPart(submitNodeCollector, armMiddle, poseStack, renderType, state);

        poseStack.translate(0, 3 / 16F, 6 / 16F);
        poseStack.mulPose(Axis.XP.rotationDegrees(angles[3]));
        poseStack.translate(0, -3 / 16F, -6 / 16F);

        submitPart(submitNodeCollector, clawBase, poseStack, renderType, state);

        poseStack.translate(0, 3 / 16F, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angles[4]));
        poseStack.translate(0, -3 / 16F, 0);

        submitPart(submitNodeCollector, clawAxle, poseStack, renderType, state);
        submitPart(submitNodeCollector, clawTurn, poseStack, renderType, state);

        poseStack.pushPose();
        poseStack.translate(state.clawTranslation, 0, 0);

        submitPart(submitNodeCollector, claw1, poseStack, renderType, state);

        poseStack.translate(-2 * state.clawTranslation, 0, 0);

        submitPart(submitNodeCollector, claw2, poseStack, renderType, state);

        poseStack.popPose();

        if (!state.heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = state.renderOverride;
            if (renderOverride == null || renderOverride.applyRenderChangeIOUnit(poseStack, state.heldStack)) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                double yOffset = state.heldStack.getItem() instanceof BlockItem ? 1.5 / 16D : 0.5 / 16D;
                poseStack.translate(0, yOffset, -3 / 16D);
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
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

    private Identifier getTexture(AssemblyIOUnitBlockEntity te) {
        return te != null && te.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT;
    }

    public static class State extends AssemblyRobotState {
        Identifier texture = Textures.MODEL_ASSEMBLY_IO_EXPORT;
        ItemStack heldStack = ItemStack.EMPTY;
        @Nullable IAssemblyRenderOverriding renderOverride;
        float clawTranslation;
        final ItemStackRenderState item = new ItemStackRenderState();
    }
}
