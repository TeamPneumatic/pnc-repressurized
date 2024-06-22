package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelDrone extends EntityModel<AbstractDroneEntity> {
    private final ModelPart drone;
    private final ModelPart prop_1;
    private final ModelPart prop_2;
    private final ModelPart prop_3;
    private final ModelPart prop_4;

    private static final String DRONE = "drone";
    private static final String BODY = "body";
    private static final String LOWER_FRAME_R1 = "lower_frame_r1";
    private static final String NORTH_WEST_WING = "north_west_wing";
    private static final String PROP_1 = "prop_1";
    private static final String BLADE3_CONNECTION_R1 = "blade3_connection_r1";
    private static final String BLADE2_CONNECTION_R1 = "blade2_connection_r1";
    private static final String BLADE1_CONNECTION_R1 = "blade1_connection_r1";
    private static final String SOUTH_WEST_WING = "south_west_wing";
    private static final String PROP_2 = "prop_2";
    private static final String BLADE6_CONNECTION_R1 = "blade6_connection_r1";
    private static final String BLADE5_CONNECTION_R1 = "blade5_connection_r1";
    private static final String BLADE4_CONNECTION_R1 = "blade4_connection_r1";
    private static final String SOUTH_EAST_WING = "south_east_wing";
    private static final String PROP_3 = "prop_3";
    private static final String BLADE9_CONNECTION_R1 = "blade9_connection_r1";
    private static final String BLADE8_CONNECTION_R1 = "blade8_connection_r1";
    private static final String BLADE7_CONNECTION_R1 = "blade7_connection_r1";
    private static final String NORTH_EAST_WING = "north_east_wing";
    private static final String PROP_4 = "prop_4";
    private static final String BLADE12_CONNECTION_R1 = "blade12_connection_r1";
    private static final String BLADE11_CONNECTION_R1 = "blade11_connection_r1";
    private static final String BLADE10_CONNECTION_R1 = "blade10_connection_r1";

    public ModelDrone(ModelPart root) {
        drone = root.getChild(DRONE);
        prop_1 = drone.getChild(NORTH_WEST_WING).getChild(PROP_1);
        prop_2 = drone.getChild(SOUTH_WEST_WING).getChild(PROP_2);
        prop_3 = drone.getChild(SOUTH_EAST_WING).getChild(PROP_3);
        prop_4 = drone.getChild(NORTH_EAST_WING).getChild(PROP_4);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition drone = partdefinition.addOrReplaceChild(DRONE, CubeListBuilder.create().texOffs(0, 0),
                PartPose.offset(0.0F, 22.5F, 0.0F));
        PartDefinition body = drone.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("body_0", -4.0F, -4.0F, -12.0F, 8, 4, 24, 0, 93)
                        .addBox("body_1", 4.0F, -4.0F, 6.0F, 2, 4, 3, 10, 121)
                        .addBox("body_2", -6.0F, -4.0F, 6.0F, 2, 4, 3, 20, 121)
                        .addBox("body_3", 4.0F, -4.0F, -9.0F, 2, 4, 3, 0, 121)
                        .addBox("body_4", -6.0F, -4.0F, -9.0F, 2, 4, 3, 30, 121)
                        .addBox("body_5", -4.5F, -3.5F, -12.5F, 9, 1, 25, 0, 67),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        body.addOrReplaceChild(LOWER_FRAME_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("lower_frame_r1_0", -4.75F, -0.75F, -43.75F, 9, 1, 25, 0, 67),
                PartPose.offsetAndRotation(-0.25F, -0.75F, -31.25F, -3.1416F, 0.0F, 3.1416F));
        PartDefinition north_west_wing = drone.addOrReplaceChild(NORTH_WEST_WING, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("north_west_wing_0", -1.0F, -1.0F, -1.0F, 7, 2, 2, 0, 113)
                        .addBox("north_west_wing_1", 4.5F, 1.0F, -0.5F, 1, 6, 1, 44, 110)
                        .addBox("north_west_wing_2", 4.5F, -3.0F, -0.5F, 1, 2, 1, 52, 107),
                PartPose.offsetAndRotation(6.0F, -5.5F, -7.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition prop_1 = north_west_wing.addOrReplaceChild(PROP_1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("prop_1_0", -0.5F, -1.5F, -0.5F, 1, 1, 1, 52, 105),
                PartPose.offset(5.0F, -2.5F, 0.0F));
        prop_1.addOrReplaceChild(BLADE3_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade3_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 52, 105)
                        .addBox("blade3_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 68, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1572F, -0.3614F, -0.4215F));
        prop_1.addOrReplaceChild(BLADE2_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade2_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 48, 105)
                        .addBox("blade2_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 54, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 2.7761F, -0.7119F, -2.6117F));
        prop_1.addOrReplaceChild(BLADE1_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade1_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 44, 105)
                        .addBox("blade1_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 40, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -1.5708F, 1.1781F, -1.5708F));
        PartDefinition south_west_wing = drone.addOrReplaceChild(SOUTH_WEST_WING, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("south_west_wing_0", -1.0F, -1.0F, -1.0F, 7, 2, 2, 0, 105)
                        .addBox("south_west_wing_1", 4.5F, 1.0F, -0.5F, 1, 6, 1, 48, 110)
                        .addBox("south_west_wing_2", 4.5F, -3.0F, -0.5F, 1, 2, 1, 48, 107),
                PartPose.offsetAndRotation(6.0F, -5.5F, 7.5F, 0.0F, -0.3927F, 0.0F));
        PartDefinition prop_2 = south_west_wing.addOrReplaceChild(PROP_2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("prop_2_0", -0.5F, -1.5F, -0.5F, 1, 1, 1, 48, 105),
                PartPose.offset(5.0F, -2.5F, 0.0F));
        prop_2.addOrReplaceChild(BLADE6_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade6_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 48, 105)
                        .addBox("blade6_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 68, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1572F, 0.3614F, -0.4215F));
        prop_2.addOrReplaceChild(BLADE5_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade5_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 52, 105)
                        .addBox("blade5_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 54, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -2.7761F, 0.7119F, -2.6117F));
        prop_2.addOrReplaceChild(BLADE4_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade4_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 40, 105)
                        .addBox("blade4_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 40, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 1.5708F, -1.1781F, -1.5708F));
        PartDefinition south_east_wing = drone.addOrReplaceChild(SOUTH_EAST_WING, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("south_east_wing_0", -6.0F, -1.0F, -1.0F, 7, 2, 2, 0, 101)
                        .addBox("south_east_wing_1", -5.5F, 1.0F, -0.5F, 1, 6, 1, 52, 110)
                        .addBox("south_east_wing_2", -5.5F, -3.0F, -0.5F, 1, 2, 1, 44, 107),
                PartPose.offsetAndRotation(-6.0F, -5.5F, 7.5F, 0.0F, 0.3927F, 0.0F));
        PartDefinition prop_3 = south_east_wing.addOrReplaceChild(PROP_3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("prop_3_0", -0.5F, -1.5F, -0.5F, 1, 1, 1, 44, 105),
                PartPose.offset(-5.0F, -2.5F, 0.0F));
        prop_3.addOrReplaceChild(BLADE9_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade9_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 40, 105)
                        .addBox("blade9_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 68, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1572F, -0.3614F, 0.4215F));
        prop_3.addOrReplaceChild(BLADE8_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade8_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 52, 105)
                        .addBox("blade8_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 68, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -2.7761F, -0.7119F, 2.6117F));
        prop_3.addOrReplaceChild(BLADE7_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade7_connection_r1_0", -0.5F, -0.5F, 0.5F, 1, 1, 1, 44, 105)
                        .addBox("blade7_connection_r1_1", -1.0F, -0.5F, 1.5F, 2, 1, 5, 54, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 1.5708F, 1.1781F, 1.5708F));
        PartDefinition north_east_wing = drone.addOrReplaceChild(NORTH_EAST_WING, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("north_east_wing_0", -6.0F, -1.0F, -1.0F, 7, 2, 2, 0, 109)
                        .addBox("north_east_wing_1", -5.5F, 1.0F, -0.5F, 1, 6, 1, 40, 110)
                        .addBox("north_east_wing_2", -5.5F, -3.0F, -0.5F, 1, 2, 1, 40, 107),
                PartPose.offsetAndRotation(-6.0F, -5.5F, -7.5F, 0.0F, -0.3927F, 0.0F));
        PartDefinition prop_4 = north_east_wing.addOrReplaceChild(PROP_4, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("prop_4_0", -0.5F, -1.5F, -0.5F, 1, 1, 1, 40, 105),
                PartPose.offset(-5.0F, -2.5F, 0.0F));
        prop_4.addOrReplaceChild(BLADE12_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade12_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 48, 105)
                        .addBox("blade12_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 40, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1572F, 0.3614F, 0.4215F));
        prop_4.addOrReplaceChild(BLADE11_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade11_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 40, 105)
                        .addBox("blade11_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 40, 93),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 2.7761F, 0.7119F, 2.6117F));
        prop_4.addOrReplaceChild(BLADE10_CONNECTION_R1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("blade10_connection_r1_0", -0.5F, -0.5F, -1.5F, 1, 1, 1, 44, 105)
                        .addBox("blade10_connection_r1_1", -1.0F, -0.5F, -6.5F, 2, 1, 5, 54, 99),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -1.5708F, -1.1781F, 1.5708F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(AbstractDroneEntity drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
        drone.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void prepareMobModel(AbstractDroneEntity drone, float par2, float par3, float partialTicks) {
        float propRotation = Mth.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        prop_1.yRot = propRotation;
        prop_2.yRot = propRotation;
        prop_3.yRot = -propRotation;
        prop_4.yRot = -propRotation;
    }
}
