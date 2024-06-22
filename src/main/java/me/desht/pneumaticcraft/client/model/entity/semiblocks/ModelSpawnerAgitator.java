package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.common.entity.semiblock.HeatFrameEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelSpawnerAgitator extends EntityModel<HeatFrameEntity> {
    private final ModelPart top1;
    private final ModelPart top2;
    private final ModelPart top3;
    private final ModelPart top4;
    private final ModelPart side1;
    private final ModelPart side2;
    private final ModelPart side3;
    private final ModelPart side4;
    private final ModelPart bottom1;
    private final ModelPart bottom2;
    private final ModelPart bottom3;
    private final ModelPart bottom4;

    private static final String TOP1 = "top1";
    private static final String TOP2 = "top2";
    private static final String TOP3 = "top3";
    private static final String TOP4 = "top4";
    private static final String SIDE1 = "side1";
    private static final String SIDE2 = "side2";
    private static final String SIDE3 = "side3";
    private static final String SIDE4 = "side4";
    private static final String BOTTOM1 = "bottom1";
    private static final String BOTTOM2 = "bottom2";
    private static final String BOTTOM3 = "bottom3";
    private static final String BOTTOM4 = "bottom4";

    public ModelSpawnerAgitator(ModelPart root) {
        top1 = root.getChild(TOP1);
        top2 = root.getChild(TOP2);
        top3 = root.getChild(TOP3);
        top4 = root.getChild(TOP4);
        side1 = root.getChild(SIDE1);
        side2 = root.getChild(SIDE2);
        side3 = root.getChild(SIDE3);
        side4 = root.getChild(SIDE4);
        bottom1 = root.getChild(BOTTOM1);
        bottom2 = root.getChild(BOTTOM2);
        bottom3 = root.getChild(BOTTOM3);
        bottom4 = root.getChild(BOTTOM4);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TOP1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top1_0", 0.0F, 0.0F, 1.0F, 17, 3, 3, 0, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 7.5F, 4.5F));
        partdefinition.addOrReplaceChild(TOP2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top2_0", 0.0F, 0.0F, 0.0F, 17, 3, 3, 0, 6)
                        .mirror(),
                PartPose.offset(-8.5F, 7.5F, -8.5F));
        partdefinition.addOrReplaceChild(TOP3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top3_0", 1.0F, 0.0F, -10.0F, 3, 3, 11, 0, 24)
                        .mirror(),
                PartPose.offset(4.5F, 7.5F, 4.5F));
        partdefinition.addOrReplaceChild(TOP4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top4_0", -1.0F, 0.0F, -10.0F, 3, 3, 11, 0, 38)
                        .mirror(),
                PartPose.offset(-7.5F, 7.5F, 4.5F));
        partdefinition.addOrReplaceChild(SIDE1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side1_0", 0.0F, -1.0F, 0.0F, 1, 11, 1, 28, 52)
                        .addBox("side1_1", 2.0F, -1.0F, 0.0F, 1, 11, 1, 32, 52)
                        .addBox("side1_2", 0.0F, -1.0F, 2.0F, 1, 11, 1, 24, 52)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(SIDE2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side2_0", 0.0F, -1.0F, 0.0F, 1, 11, 1, 40, 52)
                        .addBox("side2_1", 0.0F, -1.0F, 2.0F, 1, 11, 1, 44, 52)
                        .addBox("side2_2", -2.0F, -1.0F, 0.0F, 1, 11, 1, 36, 52)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(SIDE3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side3_0", 0.0F, -1.0F, 0.0F, 1, 11, 1, 16, 52)
                        .addBox("side3_1", 2.0F, -1.0F, 0.0F, 1, 11, 1, 12, 52)
                        .addBox("side3_2", 0.0F, -1.0F, -2.0F, 1, 11, 1, 20, 52)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(SIDE4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side4_0", 0.0F, -1.0F, 0.0F, 1, 11, 1, 4, 52)
                        .addBox("side4_1", -2.0F, -1.0F, 0.0F, 1, 11, 1, 8, 52)
                        .addBox("side4_2", 0.0F, -1.0F, -2.0F, 1, 11, 1, 0, 52)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(BOTTOM1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("bottom1_0", 0.0F, 0.0F, 1.0F, 17, 3, 3, 0, 12)
                        .mirror(),
                PartPose.offset(-8.5F, 21.5F, 4.5F));
        partdefinition.addOrReplaceChild(BOTTOM2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("bottom2_0", -1.0F, 0.0F, -10.0F, 3, 3, 11, 28, 38)
                        .mirror(),
                PartPose.offset(-7.5F, 21.5F, 4.5F));
        partdefinition.addOrReplaceChild(BOTTOM3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("bottom3_0", 1.0F, 0.0F, -10.0F, 3, 3, 11, 28, 24)
                        .mirror(),
                PartPose.offset(4.5F, 21.5F, 4.5F));
        partdefinition.addOrReplaceChild(BOTTOM4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("bottom4_0", 0.0F, 0.0F, 0.0F, 17, 3, 3, 0, 18)
                        .mirror(),
                PartPose.offset(-8.5F, 21.5F, -8.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        top1.render(matrixStack, buffer, packedLight, packedOverlay);
        top2.render(matrixStack, buffer, packedLight, packedOverlay);
        top3.render(matrixStack, buffer, packedLight, packedOverlay);
        top4.render(matrixStack, buffer, packedLight, packedOverlay);
        side1.render(matrixStack, buffer, packedLight, packedOverlay);
        side2.render(matrixStack, buffer, packedLight, packedOverlay);
        side3.render(matrixStack, buffer, packedLight, packedOverlay);
        side4.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom1.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom2.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom3.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom4.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void setupAnim(HeatFrameEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
