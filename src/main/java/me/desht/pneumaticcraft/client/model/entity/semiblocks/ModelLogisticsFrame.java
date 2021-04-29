package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelLogisticsFrame extends EntityModel<EntityLogisticsFrame> {
	private final ModelRenderer frame;
	private final ModelRenderer face;

	public ModelLogisticsFrame() {
		textureWidth = 32;
		textureHeight = 32;

		frame = new ModelRenderer(this);
		frame.setRotationPoint(8.0F, 21.0F, 0.0F);
		setRotationAngle(frame, 0.0F, -1.5708F, 0.0F);
		frame.setTextureOffset(0, 0).addBox(-6.0F, -11.0F, -1.0F, 4.0F, 12.0F, 1.0F, 0.0F, false);
		frame.setTextureOffset(20, 0).addBox(-2.0F, -11.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
		frame.setTextureOffset(10, 0).addBox(2.0F, -11.0F, -1.0F, 4.0F, 12.0F, 1.0F, 0.0F, false);
		frame.setTextureOffset(20, 5).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);

		face = new ModelRenderer(this);
		face.setRotationPoint(8.0F, 21.0F, 0.0F);
		setRotationAngle(face, 0.0F, -1.5708F, 0.0F);
		face.setTextureOffset(8, 13).addBox(2.5F, -10.5F, -1.5F, 3.0F, 11.0F, 1.0F, 0.0F, false);
		face.setTextureOffset(0, 13).addBox(-5.5F, -10.5F, -1.5F, 3.0F, 11.0F, 1.0F, 0.0F, false);
		face.setTextureOffset(16, 17).addBox(-2.5F, -2.5F, -1.5F, 5.0F, 3.0F, 1.0F, 0.0F, false);
		face.setTextureOffset(16, 13).addBox(-2.5F, -10.5F, -1.5F, 5.0F, 3.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityLogisticsFrame entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		frame.render(matrixStack, buffer, packedLight, packedOverlay);
		face.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
