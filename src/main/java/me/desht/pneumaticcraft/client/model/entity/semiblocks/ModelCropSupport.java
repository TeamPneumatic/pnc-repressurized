package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ModelCropSupport extends Model.Simple {
    private static final String STICK1 = "stick1";
    private static final String STICK2 = "stick2";
    private static final String STICK3 = "stick3";
    private static final String STICK4 = "stick4";
    private static final String STICK5 = "stick5";
    private static final String STICK6 = "stick6";
    private static final String STICK6_R1 = "stick6_r1";
    private static final String STICK7 = "stick7";
    private static final String STICK8 = "stick8";
    private static final String STICK8_R1 = "stick8_r1";

    public ModelCropSupport(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(STICK1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick1_0", 0.0F, 0.0F, 0.0F, 1, 13, 1, 48, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(STICK2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick2_0", 0.0F, 0.0F, 0.0F, 1, 13, 1, 44, 0)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, -8.5F));
        partdefinition.addOrReplaceChild(STICK3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick3_0", 0.0F, 0.0F, 0.0F, 1, 13, 1, 40, 0)
                        .mirror(),
                PartPose.offset(-8.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(STICK4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick4_0", 0.0F, 0.0F, 0.0F, 1, 13, 1, 52, 0)
                        .mirror(),
                PartPose.offset(7.5F, 11.5F, 7.5F));
        partdefinition.addOrReplaceChild(STICK5, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick5_0", -9.5F, -13.5F, -8.5F, 19, 1, 1, 0, 2)
                        .mirror(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition stick6 = partdefinition.addOrReplaceChild(STICK6, CubeListBuilder.create().texOffs(0, 0),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        stick6.addOrReplaceChild(STICK6_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick6_r1_0", -9.5F, -13.5F, -8.5F, 19, 1, 1, 0, 4)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        partdefinition.addOrReplaceChild(STICK7, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick7_0", -9.5F, -13.5F, 7.5F, 19, 1, 1, 0, 0)
                        .mirror(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition stick8 = partdefinition.addOrReplaceChild(STICK8, CubeListBuilder.create().texOffs(0, 0),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        stick8.addOrReplaceChild(STICK8_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("stick8_r1_0", -9.5F, -13.5F, 7.5F, 19, 1, 1, 0, 6)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 16);
    }
}
