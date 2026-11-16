package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer3D;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.PressureGaugeModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public class PressureGaugeRenderer extends AbstractTubeModuleRenderer<PressureGaugeModule, PressureGaugeRenderer.State> {
    private static final float GAUGE_SCALE = 0.007f;
    private static final Component BAR = Component.literal("bar");

    private final Font font;

    private final ModelPart tubeConnector1;
    private final ModelPart tubeConnector2;
    private final ModelPart faceplate;
    private final ModelPart gauge1;
    private final ModelPart gauge2;
    private final ModelPart gauge3;
    private final ModelPart gauge4;
    private final ModelPart gauge5;
    private final ModelPart gauge6;
    private final ModelPart gauge7;
    private final ModelPart gauge8;

    private static final String TUBECONNECTOR1 = "tubeConnector1";
    private static final String TUBECONNECTOR2 = "tubeConnector2";
    private static final String FACEPLATE = "faceplate";
    private static final String GAUGE1 = "gauge1";
    private static final String GAUGE2 = "gauge2";
    private static final String GAUGE3 = "gauge3";
    private static final String GAUGE4 = "gauge4";
    private static final String GAUGE5 = "gauge5";
    private static final String GAUGE6 = "gauge6";
    private static final String GAUGE7 = "gauge7";
    private static final String GAUGE8 = "gauge8";

    public PressureGaugeRenderer(BlockEntityRendererProvider.Context ctx) {
        font = ctx.font();
        ModelPart root = ctx.bakeLayer(PNCModelLayers.PRESSURE_GAUGE_MODULE);
        tubeConnector1 = root.getChild(TUBECONNECTOR1);
        tubeConnector2 = root.getChild(TUBECONNECTOR2);
        faceplate = root.getChild(FACEPLATE);
        gauge1 = root.getChild(GAUGE1);
        gauge2 = root.getChild(GAUGE2);
        gauge3 = root.getChild(GAUGE3);
        gauge4 = root.getChild(GAUGE4);
        gauge5 = root.getChild(GAUGE5);
        gauge6 = root.getChild(GAUGE6);
        gauge7 = root.getChild(GAUGE7);
        gauge8 = root.getChild(GAUGE8);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TUBECONNECTOR1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("tubeConnector1_0", 0.0F, 0.0F, 0.0F, 3, 3, 3),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR2, CubeListBuilder.create().texOffs(22, 6)
                        .addBox("tubeConnector2_0", -2.0F, -2.0F, 2.0F, 7, 7, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(FACEPLATE, CubeListBuilder.create().texOffs(0, 6)
                        .addBox("faceplate_0", -1.0F, -1.0F, 0.0F, 10, 10, 1),
                PartPose.offset(-4.0F, 12.0F, 5.0F));
        partdefinition.addOrReplaceChild(GAUGE1, CubeListBuilder.create().texOffs(0, 17)
                        .addBox("gauge1_0", -3.0F, -2.0F, 0.0F, 1, 4, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE2, CubeListBuilder.create().texOffs(4, 17)
                        .addBox("gauge2_0", 4.0F, -2.0F, 0.0F, 1, 4, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE3, CubeListBuilder.create().texOffs(8, 17)
                        .addBox("gauge3_0", 3.0F, -3.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE4, CubeListBuilder.create().texOffs(12, 17)
                        .addBox("gauge4_0", 3.0F, 2.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE5, CubeListBuilder.create().texOffs(8, 19)
                        .addBox("gauge5_0", -2.0F, -3.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE6, CubeListBuilder.create().texOffs(12, 19)
                        .addBox("gauge6_0", -2.0F, 2.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE7, CubeListBuilder.create().texOffs(0, 24)
                        .addBox("gauge7_0", -1.0F, 3.0F, 0.0F, 4, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));
        partdefinition.addOrReplaceChild(GAUGE8, CubeListBuilder.create().texOffs(0, 22)
                        .addBox("gauge8_0", -1.0F, -4.0F, 0.0F, 4, 1, 1),
                PartPose.offset(-1.0F, 16.0F, 5.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PressureGaugeModule module, State state, float partialTicks, Vec3 cameraPosition) {
        super.extractRenderState(module, state, partialTicks, cameraPosition);

        state.showDisplay = !module.isFake() && module.getTube().getBlockPos().distToCenterSqr(cameraPosition) <= 256;
        if (state.showDisplay) {
            PressureTubeBlockEntity te = module.getTube();
            state.showGauge = module.shouldShowGauge();
            state.criticalPressure = te.getCriticalPressure();
            state.dangerPressure = te.getDangerPressure();
            state.pressure = te.getPressure();
        }
    }

    @Override
    protected void submitModel(State state, PoseStack poseStack, ModuleSubmitter submitter) {
        submitter.submitPart(tubeConnector1, poseStack);
        submitter.submitPart(tubeConnector2, poseStack);
        submitter.submitPart(faceplate, poseStack);
        submitter.submitPart(gauge1, poseStack);
        submitter.submitPart(gauge2, poseStack);
        submitter.submitPart(gauge3, poseStack);
        submitter.submitPart(gauge4, poseStack);
        submitter.submitPart(gauge5, poseStack);
        submitter.submitPart(gauge6, poseStack);
        submitter.submitPart(gauge7, poseStack);
        submitter.submitPart(gauge8, poseStack);
    }

    @Override
    protected void submitExtras(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (!state.showDisplay) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(poseStack, state.direction);
        poseStack.translate(0, 1.01, 0.378);
        poseStack.scale(GAUGE_SCALE, GAUGE_SCALE, GAUGE_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        if (state.showGauge) {
            PressureGaugeRenderer3D.drawPressureGauge(poseStack, submitNodeCollector, -1, state.criticalPressure, state.dangerPressure,
                    0, state.pressure, 0, 0, 0xFF000000);
        } else {
            // plain text display of bar
            Component s = Component.literal(PneumaticCraftUtils.roundNumberTo(state.pressure, 1));
            submitText(submitNodeCollector, poseStack, s, -font.width(s) / 2f, -10);
            submitText(submitNodeCollector, poseStack, BAR, -font.width(BAR) / 2f, 0);
        }
        poseStack.popPose();
    }

    private static void submitText(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, Component text, float x, float y) {
        submitNodeCollector.submitText(poseStack, x, y, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, 0xFF000000, 0, 0);
    }

    @Override
    protected Identifier getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_GAUGE_UPGRADED : Textures.MODEL_GAUGE;
    }

    public static class State extends ModuleRenderState {
        boolean showDisplay;
        boolean showGauge;
        float criticalPressure;
        float dangerPressure;
        float pressure;
    }
}
