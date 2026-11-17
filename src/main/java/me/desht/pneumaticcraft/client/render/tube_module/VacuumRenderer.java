package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.VacuumModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VacuumRenderer extends AbstractTubeModuleRenderer<VacuumModule, VacuumRenderer.State> {
    private static final int BLADE_COUNT = 6;

    private final ModelPart mainPart;
    private final ModelPart blade;

    private static final String MAIN = "main";
    private static final String BLADE = "blade";

    public VacuumRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.VACUUM_MODULE);
        mainPart = root.getChild(MAIN);
        blade = root.getChild(BLADE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(MAIN, CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -12.0F, -7.0F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-3.0F, -11.0F, 3.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 14).addBox(3.0F, -11.0F, 3.0F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 14).addBox(-4.0F, -11.0F, 3.0F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-4.0F, -12.0F, 3.0F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-4.0F, -5.0F, 3.0F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0).addBox(-0.5F, -8.5F, -2.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(BLADE, CubeListBuilder.create()
                        .texOffs(24, 25).addBox("blade_0", 0.0F, 0.0F, -1.0F, 1, 4, 3), PartPose.offset(-0.5F, 14.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(VacuumModule module, State state, float partialTicks, Vec3 cameraPosition) {
        super.extractRenderState(module, state, partialTicks, cameraPosition);

        state.rotation = Mth.lerp(partialTicks, module.oldRotation, module.rotation);
    }

    @Override
    protected void submitModel(State state, PoseStack poseStack, ModuleSubmitter submitter) {
        submitter.submitPart(mainPart, poseStack);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.pushPose();
        poseStack.translate(0, -1.13f, 1f);
        poseStack.scale(1.75f, 1.1f, 1.75f);
        for (int i = 0; i < BLADE_COUNT; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation * 2 + (i + 0.5F) / BLADE_COUNT * 360));
            poseStack.translate(0, 0, 1D / 16D);
            submitter.submitPart(blade, poseStack);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    protected Identifier getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_VACUUM_MODULE_UPGRADED : Textures.MODEL_VACUUM_MODULE;
    }

    public static class State extends ModuleRenderState {
        float rotation;
    }
}
