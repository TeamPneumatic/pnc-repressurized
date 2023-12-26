package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class ThermostatRenderer extends AbstractTubeModuleRenderer<ThermostatModule> {
    protected static final String CONNECTOR = "connector";
    protected static final String FACEPLATE = "faceplate";
    protected static final String FRAME = "frame";

    private final ModelPart connector;
    private final ModelPart faceplate;
    private final ModelPart frame;

    public ThermostatRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.THERMOSTAT_MODULE);
        connector = root.getChild(CONNECTOR);
        faceplate = root.getChild(FACEPLATE);
        frame = root.getChild(FRAME);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition tubeConnector = partdefinition.addOrReplaceChild(CONNECTOR, CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        tubeConnector.addOrReplaceChild("tubeConnector_r1", CubeListBuilder.create().texOffs(0, 27).addBox(-2.0F, -10.0F, -4.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(1.5F, -10.5F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-3.0F, -11.0F, -4.5F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 25).addBox(1.5F, -6.5F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 25).addBox(-2.5F, -10.5F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-2.5F, -6.5F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-2.5F, -10.5F, -3.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition faceplate = partdefinition.addOrReplaceChild(FACEPLATE, CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        faceplate.addOrReplaceChild("faceplate_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -13.0F, -6.0F, 7.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition frame = partdefinition.addOrReplaceChild(FRAME, CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        frame.addOrReplaceChild("frame4_r1", CubeListBuilder.create().texOffs(18, 1).addBox(-4.0F, -13.0F, -7.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 14).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 1).addBox(3.0F, -13.0F, -7.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 12).addBox(-4.0F, -13.5F, -7.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    protected void render(ThermostatModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        connector.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);

        float[] cols = DyeColor.byId(module.getColorChannel()).getTextureDiffuseColors();
        frame.render(matrixStack, builder, combinedLight, combinedOverlay, cols[0], cols[1], cols[2], alpha);
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_THERMOSTAT_MODULE_UPGRADED : Textures.MODEL_THERMOSTAT_MODULE;
    }
}
