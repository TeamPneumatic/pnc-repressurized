package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.compressor.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SolarCompressorRenderer extends AbstractBlockEntityModelRenderer<SolarCompressorBlockEntity> {
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
    void renderModel(SolarCompressorBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_SOLAR_COMPRESSOR));

        if (!te.isBounding(te)) {
            float panelAngle = (180F / (float) Math.PI) * te.getLevel().getSunAngle(te.getLevel().getDayTime());

            // Translates panels to origin to rotate
            matrixStackIn.translate(0, -6 / 16F, 0);

            // Rotates panel with sun during the day
            if (panelAngle < 90 || panelAngle > 270) {
                matrixStackIn.mulPose(Axis.ZN.rotationDegrees(panelAngle));
            }

            // Returns panel to default position
            else {
                matrixStackIn.mulPose(Axis.ZN.rotationDegrees(90 - (panelAngle - 90)));
            }

            // Translates panels back to original position after rotation
            matrixStackIn.translate(0, 6 / 16F, 0);

            panels.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        }
    }
}
