package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.VacuumPumpBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VacuumPumpRenderer extends AbstractBlockEntityModelRenderer<VacuumPumpBlockEntity, VacuumPumpRenderer.State> {
    private static final int BLADE_COUNT = 6;

    private static final String BLADE = "blade";

    private final ModelPart blade;

    public VacuumPumpRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.VACUUM_PUMP);
        blade = root.getChild(BLADE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BLADE, CubeListBuilder.create().texOffs(24, 25)
                        .addBox("blade_0", 0.0F, 0.0F, -1.0F, 1, 4, 3),
                PartPose.offset(-0.5F, 14.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(VacuumPumpBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.bladeRotation = Mth.lerp(partialTicks, blockEntity.oldRotation, blockEntity.rotation) + 1;
    }

    @Override
    void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderType renderType = RenderTypes.entityTranslucent(Textures.MODEL_VACUUM_PUMP);

        RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        poseStack.pushPose();
        poseStack.translate(0, -0.68f, 1f);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        for (int i = 0; i < BLADE_COUNT; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.bladeRotation * 2 + (i + 0.5F) / BLADE_COUNT * 360));
            poseStack.translate(0, 0, 1D / 16D);
            submitPart(submitNodeCollector, blade, poseStack, renderType, state);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static class State extends ModelRenderState {
        float bladeRotation;
    }
}
