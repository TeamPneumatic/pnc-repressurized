package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ModelSpawnerAgitator extends Model.Simple {
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
        super(root, RenderTypes::entityCutoutCull);
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
}
