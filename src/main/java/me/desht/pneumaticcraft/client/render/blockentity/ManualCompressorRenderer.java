package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.compressor.ManualCompressorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ManualCompressorRenderer extends AbstractBlockEntityModelRenderer<ManualCompressorBlockEntity, ManualCompressorRenderer.State> {
    private final ModelPart pump;
    private final ModelPart pumprod;
    private final ModelPart compressor;
    private final ModelPart base;

    public ManualCompressorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.MANUAL_COMPRESSOR);
        this.pump = root.getChild("pump");
        this.pumprod = root.getChild("pumprod");
        this.compressor = root.getChild("compressor");
        this.base = root.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition pump = partdefinition.addOrReplaceChild("pump", CubeListBuilder.create().texOffs(13, 25).addBox(-2.0F, 0.5F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(1, 27).addBox(-3.0F, 0.5F, -2.0F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(35, 27).addBox(2.0F, 0.5F, -2.0F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(19, 20).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.5F, 0.0F));

        PartDefinition pumprod = partdefinition.addOrReplaceChild("pumprod", CubeListBuilder.create().texOffs(21, 9).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition handle_r1 = pumprod.addOrReplaceChild("handle_r1", CubeListBuilder.create().texOffs(12, 3).addBox(-5.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition compressor = partdefinition.addOrReplaceChild("compressor", CubeListBuilder.create().texOffs(62, 29).addBox(-2.0F, -10.0F, 2.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 16).addBox(-3.0F, -11.0F, 3.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 8).addBox(-2.0F, -10.0F, 7.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition support_r1 = compressor.addOrReplaceChild("support_r1", CubeListBuilder.create().texOffs(79, 22).addBox(-0.5F, -1.5F, -0.499F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -4.0F, 4.5F, 0.0F, 0.0F, 0.3927F));

        PartDefinition support_r2 = compressor.addOrReplaceChild("support_r2", CubeListBuilder.create().texOffs(51, 22).addBox(-0.5F, -1.5F, -0.499F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -4.0F, 4.5F, 0.0F, 0.0F, -0.3927F));

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(30, 43).addBox(-5.0F, -3.0F, 4.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 51).addBox(-5.0F, -3.0F, -6.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(1, 42).addBox(3.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(47, 42).addBox(-5.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(22, 63).addBox(-3.0F, -1.0F, -7.0F, 6.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ManualCompressorBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (blockEntity.getLevel().getGameTime() <= blockEntity.pumpStepStartTick + ManualCompressorBlockEntity.TICKS_PER_PUMP_STEP) {
            // intermediate offsets of the pump rod during its animation
            state.pumprodOffset = (float) Mth.lerp(partialTicks, blockEntity.pumprodVerticalOffsetPreviousTick, blockEntity.pumprodVerticalOffsetCurrentTick);
        } else {
            // final stationary offset of the pump rod after animation is complete
            state.pumprodOffset = (float) blockEntity.pumprodVerticalOffsetCurrent;
        }
    }

    @Override
    void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Rotates the pumprod to match the rotation of the compressor
        poseStack.translate(0, -9 / 16F, 0);

        switch (state.rotation) {
            case NORTH -> poseStack.mulPose(Axis.YN.rotationDegrees(0));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(270));
            case SOUTH -> poseStack.mulPose(Axis.YN.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YN.rotationDegrees(90));
        }

        poseStack.translate(0, 9 / 16F, 0);
        poseStack.translate(0, state.pumprodOffset / 16F, 0);

        submitPart(submitNodeCollector, pumprod, poseStack, RenderTypes.entityCutout(Textures.MODEL_MANUAL_COMPRESSOR), state);
    }

    public static class State extends ModelRenderState {
        float pumprodOffset;
    }
}
