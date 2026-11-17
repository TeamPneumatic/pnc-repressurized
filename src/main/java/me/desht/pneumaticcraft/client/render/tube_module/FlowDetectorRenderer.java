package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.FlowDetectorModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FlowDetectorRenderer extends AbstractTubeModuleRenderer<FlowDetectorModule, FlowDetectorRenderer.State> {
    private static final int TUBE_PARTS = 4;

    private final ModelPart face;

    private static final String FACE = "face";

    public FlowDetectorRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.FLOW_DETECTOR_MODULE);
        face = root.getChild(FACE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(FACE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("face_0", -2.0F, -3.0F, -2.0F, 4, 1, 5),
                PartPose.offset(0.0F, 16.0F, 4.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FlowDetectorModule module, State state, float partialTicks, Vec3 cameraPosition) {
        super.extractRenderState(module, state, partialTicks, cameraPosition);

        state.rotation = Mth.lerp(partialTicks, module.oldRotation, module.rotation);
    }

    @Override
    protected void submitModel(State state, PoseStack poseStack, ModuleSubmitter submitter) {
        // parts are rendered later, so rotate the pose stack around the part's pivot instead of changing face.zRot
        for (int i = 0; i < TUBE_PARTS; i++) {
            poseStack.pushPose();
            poseStack.translate(face.x / 16f, face.y / 16f, face.z / 16f);
            poseStack.mulPose(Axis.ZP.rotation((float)i / TUBE_PARTS * 2 * (float)Math.PI + state.rotation));
            poseStack.translate(-face.x / 16f, -face.y / 16f, -face.z / 16f);
            submitter.submitPart(face, poseStack, -1);
            poseStack.popPose();
        }
    }

    @Override
    protected Identifier getTexture(boolean isUpgraded) {
        return Textures.MODEL_FLOW_DETECTOR;
    }

    public static class State extends ModuleRenderState {
        float rotation;
    }
}
