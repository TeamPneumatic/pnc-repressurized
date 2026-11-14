package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.compressor.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SolarCompressorRenderer extends AbstractBlockEntityModelRenderer<SolarCompressorBlockEntity, SolarCompressorRenderer.State> {
    private final ModelPart panels;
    private final ModelPart support;
    private final ModelPart base;

    public SolarCompressorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.SOLAR_COMPRESSOR);
        this.panels = root.getChild("panels");
        this.support = root.getChild("support");
        this.base = root.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition panels = partdefinition.addOrReplaceChild("panels", CubeListBuilder.create().texOffs(14, 53).addBox(-2.0F, -1.5F, 0.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 44).addBox(-2.0F, -1.5F, -2.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(15, 42).addBox(-8.0F, -0.5F, 2.0F, 16.0F, 1.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(15, 17).addBox(-8.0F, -0.5F, -22.0F, 16.0F, 1.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition support = partdefinition.addOrReplaceChild("support", CubeListBuilder.create().texOffs(3, 42).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 105).addBox(-8.0F, -16.0F, -4.0F, 16.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(43, 68).addBox(-3.0F, -17.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(5.0F, -17.0F, -5.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(24, 67).addBox(-6.0F, -17.0F, -5.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(52, 69).addBox(-5.0F, -13.0F, -8.0F, 10.0F, 10.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(50, 100).addBox(-7.0F, -15.0F, -7.0F, 14.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 81).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SolarCompressorBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.bounding = blockEntity.isBounding(blockEntity);
        state.panelAngle = blockEntity.getLevel().environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, blockEntity.getBlockPos());
    }

    @Override
    void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.bounding) {
            // Translates panels to origin to rotate
            poseStack.translate(0, -6 / 16F, 0);

            // Rotates panel with sun during the day
            if (state.panelAngle < 90 || state.panelAngle > 270) {
                poseStack.mulPose(Axis.ZN.rotationDegrees(state.panelAngle));
            }

            // Returns panel to default position
            else {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90 - (state.panelAngle - 90)));
            }

            // Translates panels back to original position after rotation
            poseStack.translate(0, 6 / 16F, 0);

            submitPart(submitNodeCollector, panels, poseStack, RenderTypes.entityCutout(Textures.MODEL_SOLAR_COMPRESSOR), state);
        }
    }

    public static class State extends ModelRenderState {
        boolean bounding;
        float panelAngle;
    }
}
