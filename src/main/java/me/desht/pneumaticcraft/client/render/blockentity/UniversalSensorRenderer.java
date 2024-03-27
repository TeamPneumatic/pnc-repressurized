package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.UniversalSensorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class UniversalSensorRenderer extends AbstractBlockEntityModelRenderer<UniversalSensorBlockEntity> {
    private final ModelPart part1;
    private final ModelPart part2;
    private final ModelPart part3;
    private final ModelPart part4;
    private final ModelPart part5;
    private final ModelPart part6;
    private final ModelPart part7;
    private final ModelPart part8;
    private final ModelPart part9;
    private final ModelPart part10;
    private final ModelPart part11;
    private final ModelPart part12;
    private final ModelPart part13;
    private final ModelPart part14;
    private final ModelPart part15;

    private static final String PART1 = "part1";
    private static final String PART2 = "part2";
    private static final String PART3 = "part3";
    private static final String PART4 = "part4";
    private static final String PART5 = "part5";
    private static final String PART6 = "part6";
    private static final String PART7 = "part7";
    private static final String PART8 = "part8";
    private static final String PART8_R1 = "part8_r1";
    private static final String PART9 = "part9";
    private static final String PART9_R1 = "part9_r1";
    private static final String PART10 = "part10";
    private static final String PART10_R1 = "part10_r1";
    private static final String PART11 = "part11";
    private static final String PART11_R1 = "part11_r1";
    private static final String PART12 = "part12";
    private static final String PART12_R1 = "part12_r1";
    private static final String PART13 = "part13";
    private static final String PART13_R1 = "part13_r1";
    private static final String PART14 = "part14";
    private static final String PART14_R1 = "part14_r1";
    private static final String PART15 = "part15";
    private static final String PART15_R1 = "part15_r1";

    public UniversalSensorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.UNIVERSAL_SENSOR);
        part1 = root.getChild(PART1);
        part2 = root.getChild(PART2);
        part3 = root.getChild(PART3);
        part4 = root.getChild(PART4);
        part5 = root.getChild(PART5);
        part6 = root.getChild(PART6);
        part7 = root.getChild(PART7);
        part8 = root.getChild(PART8);
        part9 = root.getChild(PART9);
        part10 = root.getChild(PART10);
        part11 = root.getChild(PART11);
        part12 = root.getChild(PART12);
        part13 = root.getChild(PART13);
        part14 = root.getChild(PART14);
        part15 = root.getChild(PART15);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(PART1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("part1_0", -2.0F, 0.0F, -2.0F, 4, 1, 4, 0, 0)
                        .mirror(),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        partdefinition.addOrReplaceChild(PART2, CubeListBuilder.create().texOffs(0, 9)
                        .addBox("part2_0", -3.0F, 4.0F, 0.5F, 1, 3, 3, 0, 9)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        partdefinition.addOrReplaceChild(PART3, CubeListBuilder.create().texOffs(16, 3)
                        .addBox("part3_0", -2.0F, 1.25F, -0.5F, 6, 1, 1, 16, 3)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.2269F));
        partdefinition.addOrReplaceChild(PART4, CubeListBuilder.create().texOffs(16, 0)
                        .addBox("part4_0", 3.25F, 1.25F, -1.0F, 1, 1, 2, 16, 0)
                        .mirror(),
                PartPose.offset(0.0F, 10.2F, 0.0F));
        partdefinition.addOrReplaceChild(PART5, CubeListBuilder.create().texOffs(0, 5)
                        .addBox("part5_0", -3.0F, 3.0F, 0.5F, 1, 1, 3, 0, 5)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        partdefinition.addOrReplaceChild(PART6, CubeListBuilder.create().texOffs(0, 5)
                        .addBox("part6_0", -3.0F, -2.0F, 0.5F, 1, 1, 3, 0, 5)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        partdefinition.addOrReplaceChild(PART7, CubeListBuilder.create().texOffs(18, 9)
                        .addBox("part7_0", -2.5F, -1.0F, 0.5F, 0, 4, 3, 18, 9)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        PartDefinition part8 = partdefinition.addOrReplaceChild(PART8, CubeListBuilder.create().texOffs(20, 6),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        part8.addOrReplaceChild(PART8_R1, CubeListBuilder.create().texOffs(20, 6)
                        .addBox("part8_r1_0", 0.0F, -0.5F, 0.0F, 1, 1, 5, 20, 6)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, 3.5F, 3.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition part9 = partdefinition.addOrReplaceChild(PART9, CubeListBuilder.create().texOffs(20, 6),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        part9.addOrReplaceChild(PART9_R1, CubeListBuilder.create().texOffs(20, 6)
                        .addBox("part9_r1_0", 0.0F, -0.5F, 0.0F, 1, 1, 5, 20, 6)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, -1.5F, 3.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition part10 = partdefinition.addOrReplaceChild(PART10, CubeListBuilder.create().texOffs(15, 2),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        part10.addOrReplaceChild(PART10_R1, CubeListBuilder.create().texOffs(15, 2)
                        .addBox("part10_r1_0", 0.0F, -0.5F, 0.0F, 0, 4, 5, 15, 2)
                        .mirror(),
                PartPose.offsetAndRotation(-2.5F, -0.5F, 3.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition part11 = partdefinition.addOrReplaceChild(PART11, CubeListBuilder.create().texOffs(8, 6),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, 0.0F, 0.0F, -0.2269F));
        part11.addOrReplaceChild(PART11_R1, CubeListBuilder.create().texOffs(8, 6)
                        .addBox("part11_r1_0", 0.0F, -0.5F, -5.0F, 1, 1, 5, 8, 6)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, 3.5F, -3.5F, 0.0F, -0.3927F, 0.0F));
        PartDefinition part12 = partdefinition.addOrReplaceChild(PART12, CubeListBuilder.create().texOffs(8, 6),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, 0.0F, 0.0F, -0.2269F));
        part12.addOrReplaceChild(PART12_R1, CubeListBuilder.create().texOffs(8, 6)
                        .addBox("part12_r1_0", 0.0F, -0.5F, -5.0F, 1, 1, 5, 8, 6)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, -1.5F, -3.5F, 0.0F, -0.3927F, 0.0F));
        PartDefinition part13 = partdefinition.addOrReplaceChild(PART13, CubeListBuilder.create().texOffs(8, 7),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, 0.0F, 0.0F, -0.2269F));
        part13.addOrReplaceChild(PART13_R1, CubeListBuilder.create().texOffs(8, 7)
                        .addBox("part13_r1_0", 0.0F, -0.5F, -5.0F, 0, 4, 5, 8, 7)
                        .mirror(),
                PartPose.offsetAndRotation(-2.5F, -0.5F, -3.5F, 0.0F, -0.3927F, 0.0F));
        PartDefinition part14 = partdefinition.addOrReplaceChild(PART14, CubeListBuilder.create().texOffs(28, 12),
                PartPose.offsetAndRotation(0.0F, 9.0F, -2.0F, 0.0F, 0.0F, -0.2269F));
        part14.addOrReplaceChild(PART14_R1, CubeListBuilder.create().texOffs(28, 12)
                        .addBox("part14_r1_0", 0.0F, -3.5F, 4.0F, 1, 4, 1, 28, 12)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, 2.5F, 3.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition part15 = partdefinition.addOrReplaceChild(PART15, CubeListBuilder.create().texOffs(28, 12),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, 0.0F, 0.0F, -0.2269F));
        part15.addOrReplaceChild(PART15_R1, CubeListBuilder.create().texOffs(28, 12)
                        .addBox("part15_r1_0", 0.0F, -3.5F, -5.0F, 1, 4, 1, 28, 12)
                        .mirror(),
                PartPose.offsetAndRotation(-3.0F, 2.5F, -3.5F, 0.0F, -0.3927F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void renderModel(UniversalSensorBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_UNIVERSAL_SENSOR));

        float rotation = Mth.lerp(partialTicks, te.oldDishRotation, te.dishRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotation));
        part1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part6.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part7.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part8.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part9.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part10.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part11.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part12.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part13.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part14.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part15.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }

    @Override
    public AABB getRenderBoundingBox(UniversalSensorBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
