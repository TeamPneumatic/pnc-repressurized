package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ModelHeatFrame extends Model.Simple {
    private static final String BOTTOM = "bottom";
    private static final String SIDE1 = "side1";
    private static final String SIDE2 = "side2";
    private static final String SIDE3 = "side3";
    private static final String SIDE4 = "side4";
    private static final String TOPCORNER1 = "topCorner1";
    private static final String TOPCORNER2 = "topCorner2";
    private static final String TOPCORNER3 = "topCorner3";
    private static final String TOPCORNER4 = "topCorner4";
    private static final String TOP1 = "top1";
    private static final String TOP2 = "top2";
    private static final String TOP3 = "top3";
    private static final String TOP4 = "top4";
    private static final String TOP5 = "top5";
    private static final String TOP6 = "top6";
    private static final String TOP7 = "top7";
    private static final String TOP8 = "top8";
    private static final String TOP9 = "top9";
    private static final String TOP10 = "top10";
    private static final String TOP11 = "top11";
    private static final String TOP12 = "top12";

    public ModelHeatFrame(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BOTTOM, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("bottom_0", -13.0F, -1.0F, 0.0F, 17, 5, 17, 0, 0)
                        .mirror(),
                PartPose.offset(4.5F, 20.5F, -8.5F));
        partdefinition.addOrReplaceChild(SIDE1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side1_0", 0.0F, 0.0F, 0.0F, 1, 8, 1, 0, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(SIDE2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side2_0", 0.0F, 0.0F, 0.0F, 1, 8, 1, 4, 0)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(SIDE3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side3_0", 0.0F, 0.0F, 0.0F, 1, 8, 1, 12, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(SIDE4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("side4_0", 0.0F, 0.0F, 0.0F, 1, 8, 1, 8, 0)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(TOPCORNER1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("topCorner1_0", 0.0F, 0.0F, 0.0F, 4, 4, 4, 48, 24)
                        .mirror(),
                PartPose.offset(-8.5F, 7.5F, 4.5F));
        partdefinition.addOrReplaceChild(TOPCORNER2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("topCorner2_0", 0.0F, 0.0F, 0.0F, 4, 4, 4, 0, 24)
                        .mirror(),
                PartPose.offset(4.5F, 7.5F, -8.5F));
        partdefinition.addOrReplaceChild(TOPCORNER3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("topCorner3_0", 0.0F, 0.0F, 0.0F, 4, 4, 4, 16, 24)
                        .mirror(),
                PartPose.offset(-8.5F, 7.5F, -8.5F));
        partdefinition.addOrReplaceChild(TOPCORNER4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("topCorner4_0", 0.0F, 0.0F, 0.0F, 4, 4, 4, 32, 24)
                        .mirror(),
                PartPose.offset(4.5F, 7.5F, 4.5F));
        partdefinition.addOrReplaceChild(TOP1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top1_0", 0.0F, 0.0F, 0.0F, 9, 2, 1, 64, 26)
                        .mirror(),
                PartPose.offset(-4.5F, 7.5F, 7.5F));
        partdefinition.addOrReplaceChild(TOP2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top2_0", 0.0F, 0.0F, 0.0F, 9, 2, 1, 64, 29)
                        .mirror(),
                PartPose.offset(-4.5F, 7.5F, -8.5F));
        partdefinition.addOrReplaceChild(TOP3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top3_0", 0.0F, 0.0F, 0.0F, 1, 2, 9, 51, 0)
                        .mirror(),
                PartPose.offset(7.5F, 7.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top4_0", 0.0F, 0.0F, 0.0F, 1, 2, 9, 71, 10)
                        .mirror(),
                PartPose.offset(-8.5F, 7.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP5, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top5_0", 0.0F, 0.0F, 0.0F, 1, 1, 9, 91, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 9.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP6, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top6_0", 0.0F, 0.0F, 0.0F, 1, 1, 9, 71, 0)
                        .mirror(),
                PartPose.offset(7.5F, 9.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP7, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top7_0", 0.0F, 0.0F, 0.0F, 9, 1, 1, 84, 30)
                        .mirror(),
                PartPose.offset(-4.5F, 9.5F, -8.5F));
        partdefinition.addOrReplaceChild(TOP8, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top8_0", 0.0F, 0.0F, 0.0F, 9, 1, 1, 84, 28)
                        .mirror(),
                PartPose.offset(-4.5F, 9.5F, 7.5F));
        partdefinition.addOrReplaceChild(TOP9, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top9_0", 0.0F, 1.0F, 0.0F, 1, 1, 9, 91, 10)
                        .mirror(),
                PartPose.offset(-8.5F, 9.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP10, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top10_0", 0.0F, 1.0F, 0.0F, 1, 1, 9, 102, 5)
                        .mirror(),
                PartPose.offset(7.5F, 9.5F, -4.5F));
        partdefinition.addOrReplaceChild(TOP11, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top11_0", 0.0F, 1.0F, 0.0F, 9, 1, 1, 84, 26)
                        .mirror(),
                PartPose.offset(-4.5F, 9.5F, 7.5F));
        partdefinition.addOrReplaceChild(TOP12, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("top12_0", 0.0F, 1.0F, 0.0F, 9, 1, 1, 84, 24)
                        .mirror(),
                PartPose.offset(-4.5F, 9.5F, -8.5F));

        return LayerDefinition.create(meshdefinition, 128, 32);
    }
}
