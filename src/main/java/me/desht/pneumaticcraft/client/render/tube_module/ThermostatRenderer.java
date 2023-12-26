package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public class ThermostatRenderer extends AbstractTubeModuleRenderer<ThermostatModule> {
    private final ModelPart faceplate;
    private final ModelPart tubeConnector1;
    private final ModelPart tubeConnector2;
    private final ModelPart tubeConnector3;
    private final ModelPart tubeConnector4;
    private final ModelPart tubeConnector5;
    private final ModelPart tubeConnector6;
    private final ModelPart frame1;
    private final ModelPart frame2;
    private final ModelPart frame3;
    private final ModelPart frame4;

    private static final String FACEPLATE = "faceplate";
    private static final String TUBECONNECTOR1 = "tubeConnector1";
    private static final String TUBECONNECTOR2 = "tubeConnector2";
    private static final String TUBECONNECTOR3 = "tubeConnector3";
    private static final String TUBECONNECTOR4 = "tubeConnector4";
    private static final String TUBECONNECTOR5 = "tubeConnector5";
    private static final String TUBECONNECTOR6 = "tubeConnector6";
    private static final String FRAME1 = "frame1";
    private static final String FRAME2 = "frame2";
    private static final String FRAME3 = "frame3";
    private static final String FRAME4 = "frame4";

    public ThermostatRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.THERMOSTAT_MODULE);
        faceplate = root.getChild(FACEPLATE);
        tubeConnector1 = root.getChild(TUBECONNECTOR1);
        tubeConnector2 = root.getChild(TUBECONNECTOR2);
        tubeConnector3 = root.getChild(TUBECONNECTOR3);
        tubeConnector4 = root.getChild(TUBECONNECTOR4);
        tubeConnector5 = root.getChild(TUBECONNECTOR5);
        tubeConnector6 = root.getChild(TUBECONNECTOR6);
        frame1 = root.getChild(FRAME1);
        frame2 = root.getChild(FRAME2);
        frame3 = root.getChild(FRAME3);
        frame4 = root.getChild(FRAME4);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(FACEPLATE, CubeListBuilder.create().texOffs(12, 0)
                        .addBox("faceplate_0", 0.0F, 0.0F, -1.0F, 8, 8, 2),
                PartPose.offset(-4.0F, 12.0F, 5.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR1, CubeListBuilder.create().texOffs(12, 10)
                        .addBox("tubeConnector1_0", -2.0F, -2.0F, 1.0F, 7, 7, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR2, CubeListBuilder.create().texOffs(12, 18)
                        .addBox("tubeConnector2_0", -1.0F, -1.0F, 0.0F, 5, 5, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR3, CubeListBuilder.create().texOffs(28, 12)
                        .addBox("tubeConnector3_0", 4.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR4, CubeListBuilder.create().texOffs(28, 16)
                        .addBox("tubeConnector4_0", 0.0F, 4.0F, 0.0F, 3, 1, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR5, CubeListBuilder.create().texOffs(32, 12)
                        .addBox("tubeConnector5_0", -2.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR6, CubeListBuilder.create().texOffs(28, 10)
                        .addBox("tubeConnector6_0", 0.0F, -2.0F, 0.0F, 3, 1, 1),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(FRAME1, CubeListBuilder.create().texOffs(32, 0)
                        .addBox("frame1_0", 2.0F, 1.5F, -3.75F, 4, 1, 4),
                PartPose.offset(-4.0F, 11.5F, 6.0F));
        partdefinition.addOrReplaceChild(FRAME2, CubeListBuilder.create().texOffs(32, 5)
                        .addBox("frame2_0", 2.0F, -1.5F, -3.75F, 4, 1, 4),
                PartPose.offset(-4.0F, 19.5F, 6.0F));
        partdefinition.addOrReplaceChild(FRAME3, CubeListBuilder.create().texOffs(0, 6)
                        .addBox("frame3_0", -1.5F, 0.5F, -3.75F, 1, 6, 4),
                PartPose.offset(3.5F, 12.5F, 6.0F));
        partdefinition.addOrReplaceChild(FRAME4, CubeListBuilder.create().texOffs(0, 16)
                        .addBox("frame4_0", 1.5F, 0.5F, -3.75F, 1, 6, 4),
                PartPose.offset(-4.5F, 12.5F, 6.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }


    @Override
    protected void render(ThermostatModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        tubeConnector1.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector2.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector3.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector4.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector5.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector6.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);

        float[] cols = { 1f, 1f, 1f, 1f };
        cols = DyeColor.byId(module.getColorChannel()).getTextureDiffuseColors();
        frame1.render(matrixStack, builder, combinedLight, combinedOverlay, cols[0], cols[1], cols[2], alpha);
        frame2.render(matrixStack, builder, combinedLight, combinedOverlay, cols[0], cols[1], cols[2], alpha);
        frame3.render(matrixStack, builder, combinedLight, combinedOverlay, cols[0], cols[1], cols[2], alpha);
        frame4.render(matrixStack, builder, combinedLight, combinedOverlay, cols[0], cols[1], cols[2], alpha);
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_THERMOSTAT_MODULE_UPGRADED : Textures.MODEL_THERMOSTAT_MODULE;
    }
}
