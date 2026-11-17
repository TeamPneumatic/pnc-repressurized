package me.desht.pneumaticcraft.client.model.entity.drone;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ModelDroneCore extends Model.Simple {
    private static final String DRONE = "drone";
    private static final String BODY = "body";

    public ModelDroneCore(ModelPart root) {
        super(root, RenderTypes::entityCutout);
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
}
