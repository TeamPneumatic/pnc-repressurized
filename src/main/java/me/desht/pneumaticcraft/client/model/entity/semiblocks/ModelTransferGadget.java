package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ModelTransferGadget extends Model.Simple {
    private static final String INPART1 = "inPart1";
    private static final String INPART2 = "inPart2";
    private static final String BETWEENPART = "betweenPart";
    private static final String OUTPART1 = "outPart1";
    private static final String OUTPART2 = "outPart2";

    public ModelTransferGadget(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(INPART1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("inPart1_0", -1.5F, 2.0F, -4.0F, 1, 8, 8, 32, 0),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild(INPART2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("inPart2_0", 0.5F, 4.0F, -2.0F, 1, 4, 4, 30, 0),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild(BETWEENPART, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("betweenPart_0", -0.5F, 3.0F, -3.0F, 1, 6, 6, 18, 4),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPART1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("outPart1_0", 0.5F, 2.0F, -4.0F, 1, 8, 8, 0, 0),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPART2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("outPart2_0", 0.5F, 4.0F, -2.0F, 1, 4, 4, 10, 0),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 16);
    }
}
