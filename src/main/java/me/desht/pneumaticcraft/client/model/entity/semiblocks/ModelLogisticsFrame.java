package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelLogisticsFrame extends EntityModel<AbstractLogisticsFrameEntity> {
	private final ModelPart frame;
	private final ModelPart face;

	private static final String FRAME = "frame";
	private static final String FACE = "face";

	public ModelLogisticsFrame(ModelPart root) {
		frame = root.getChild(FRAME);
		face = root.getChild(FACE);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild(FRAME, CubeListBuilder.create().texOffs(0, 0)
						.addBox("frame_0", -6.0F, -11.0F, -1.0F, 4, 12, 1, 0, 0)
						.addBox("frame_1", -2.0F, -11.0F, -1.0F, 4, 4, 1, 20, 0)
						.addBox("frame_2", 2.0F, -11.0F, -1.0F, 4, 12, 1, 10, 0)
						.addBox("frame_3", -2.0F, -3.0F, -1.0F, 4, 4, 1, 20, 5),
				PartPose.offsetAndRotation(8.0F, 21.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
		partdefinition.addOrReplaceChild(FACE, CubeListBuilder.create().texOffs(0, 0)
						.addBox("face_0", 2.5F, -10.5F, -1.5F, 3, 11, 1, 8, 13)
						.addBox("face_1", -5.5F, -10.5F, -1.5F, 3, 11, 1, 0, 13)
						.addBox("face_2", -2.5F, -2.5F, -1.5F, 5, 3, 1, 16, 17)
						.addBox("face_3", -2.5F, -10.5F, -1.5F, 5, 3, 1, 16, 13),
				PartPose.offsetAndRotation(8.0F, 21.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}


	@Override
	public void setupAnim(AbstractLogisticsFrameEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		frame.render(matrixStack, buffer, packedLight, packedOverlay, color);
		face.render(matrixStack, buffer, packedLight, packedOverlay, color);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
