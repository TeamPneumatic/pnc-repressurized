package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.ManualCompressorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class ManualCompressorRenderer extends AbstractBlockEntityModelRenderer<ManualCompressorBlockEntity> {
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
    void renderModel(ManualCompressorBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_MANUAL_COMPRESSOR));

        // Rotates the pumprod to match the rotation of the compressor
        matrixStackIn.translate(0, -9 / 16F, 0);

        switch (te.getRotation()) {
            case NORTH -> matrixStackIn.mulPose(Axis.YN.rotationDegrees(0));
            case EAST -> matrixStackIn.mulPose(Axis.YN.rotationDegrees(270));
            case SOUTH -> matrixStackIn.mulPose(Axis.YN.rotationDegrees(180));
            case WEST -> matrixStackIn.mulPose(Axis.YN.rotationDegrees(90));
        }

        matrixStackIn.translate(0, 9 / 16F, 0);

        // Renders the intermediate offsets of the pump rod during its animation
        if (te.getLevel().getGameTime() <= te.pumpStepStartTick + ManualCompressorBlockEntity.TICKS_PER_PUMP_STEP) {
            double offset = Mth.lerp(partialTicks, te.pumprodVerticalOffsetPreviousTick, te.pumprodVerticalOffsetCurrentTick);
            matrixStackIn.translate(0, offset / 16F, 0);
        }

        // Renders the final stationary offset of the pump rod after animation is complete
        else {
            matrixStackIn.translate(0, te.pumprodVerticalOffsetCurrent / 16F, 0);
        }

        pumprod.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
