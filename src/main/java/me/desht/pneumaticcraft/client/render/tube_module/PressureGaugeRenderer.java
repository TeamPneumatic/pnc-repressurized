package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer3D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PressureGaugeRenderer extends AbstractTubeModuleRenderer<PressureGaugeModule> {
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
        font = ctx.getFont();
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
    protected void render(PressureGaugeModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        tubeConnector1.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector2.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge1.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge2.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge3.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge4.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge5.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge6.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge7.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        gauge8.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
    }

    @Override
    public void renderExtras(PressureGaugeModule module, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        if (module.isFake()) return;

        BlockPos pos = module.getTube().getBlockPos();
        if (ClientUtils.getClientPlayer().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 256) return;

        matrixStack.pushPose();

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        PressureTubeBlockEntity te = module.getTube();
        matrixStack.translate(0, 1.01, 0.378);
        matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE, GAUGE_SCALE);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        if (module.shouldShowGauge()) {
            PressureGaugeRenderer3D.drawPressureGauge(matrixStack, buffer, -1, te.getCriticalPressure(), te.getDangerPressure(),
                    0, te.getPressure(), 0, 0, 0xFF000000);
        } else {
            // plain text display of bar
            Component s = Component.literal(PneumaticCraftUtils.roundNumberTo(te.getPressure(), 1));
            RenderUtils.renderString3d(s, -font.width(s) / 2f, -10, 0x000000, matrixStack, buffer, false, false);
            RenderUtils.renderString3d(BAR, -font.width(BAR) / 2f, 0, 0x000000, matrixStack, buffer, false, false);
        }
        matrixStack.popPose();
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_GAUGE_UPGRADED : Textures.MODEL_GAUGE;
    }
}
