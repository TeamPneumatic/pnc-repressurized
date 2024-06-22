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

public class ModelDroneCore extends EntityModel<AbstractDroneEntity> {
    private final ModelPart drone;

    private static final String DRONE = "drone";
    private static final String BODY = "body";

    public ModelDroneCore(ModelPart root) {
        drone = root.getChild(DRONE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition done = partdefinition.addOrReplaceChild(DRONE, CubeListBuilder.create().texOffs(0, 0),
                PartPose.offset(0.0F, 22.5F, 0.0F));
        done.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("body_0", -3.5F, -5.0F, -4.5F, 7, 6, 16, 48, 106),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(AbstractDroneEntity drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
        drone.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
    }
}
