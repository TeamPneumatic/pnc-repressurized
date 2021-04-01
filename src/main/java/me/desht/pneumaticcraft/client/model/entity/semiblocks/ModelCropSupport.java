package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityCropSupport;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelCropSupport extends EntityModel<EntityCropSupport> {
    private final ModelRenderer stick1;
    private final ModelRenderer stick2;
    private final ModelRenderer stick3;
    private final ModelRenderer stick4;
    private final ModelRenderer stick5;
    private final ModelRenderer stick6;
    private final ModelRenderer stick6_r1;
    private final ModelRenderer stick7;
    private final ModelRenderer stick8;
    private final ModelRenderer stick8_r1;

    public ModelCropSupport() {
        textureWidth = 64;
        textureHeight = 16;

        stick1 = new ModelRenderer(this);
        stick1.setPos(-8.5F, 11.5F, -8.5F);
        stick1.texOffs(48, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 13.0F, 1.0F, 0.0F, true);

        stick2 = new ModelRenderer(this);
        stick2.setPos(7.5F, 11.5F, -8.5F);
        stick2.texOffs(44, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 13.0F, 1.0F, 0.0F, true);

        stick3 = new ModelRenderer(this);
        stick3.setPos(-8.5F, 11.5F, 7.5F);
        stick3.texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 13.0F, 1.0F, 0.0F, true);

        stick4 = new ModelRenderer(this);
        stick4.setPos(7.5F, 11.5F, 7.5F);
        stick4.texOffs(52, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 13.0F, 1.0F, 0.0F, true);

        stick5 = new ModelRenderer(this);
        stick5.setPos(0.0F, 24.0F, 0.0F);
        stick5.texOffs(0, 2).addBox(-9.5F, -13.5F, -8.5F, 19.0F, 1.0F, 1.0F, 0.0F, true);

        stick6 = new ModelRenderer(this);
        stick6.setPos(0.0F, 24.0F, 0.0F);


        stick6_r1 = new ModelRenderer(this);
        stick6_r1.setPos(0.0F, 0.0F, 0.0F);
        stick6.addChild(stick6_r1);
        setRotationAngle(stick6_r1, 0.0F, -1.5708F, 0.0F);
        stick6_r1.texOffs(0, 4).addBox(-9.5F, -13.5F, -8.5F, 19.0F, 1.0F, 1.0F, 0.0F, true);

        stick7 = new ModelRenderer(this);
        stick7.setPos(0.0F, 24.0F, 0.0F);
        stick7.texOffs(0, 0).addBox(-9.5F, -13.5F, 7.5F, 19.0F, 1.0F, 1.0F, 0.0F, true);

        stick8 = new ModelRenderer(this);
        stick8.setPos(0.0F, 24.0F, 0.0F);


        stick8_r1 = new ModelRenderer(this);
        stick8_r1.setPos(0.0F, 0.0F, 0.0F);
        stick8.addChild(stick8_r1);
        setRotationAngle(stick8_r1, 0.0F, -1.5708F, 0.0F);
        stick8_r1.texOffs(0, 6).addBox(-9.5F, -13.5F, 7.5F, 19.0F, 1.0F, 1.0F, 0.0F, true);
    }

    @Override
    public void setupAnim(EntityCropSupport entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        stick1.render(matrixStack, buffer, packedLight, packedOverlay);
        stick2.render(matrixStack, buffer, packedLight, packedOverlay);
        stick3.render(matrixStack, buffer, packedLight, packedOverlay);
        stick4.render(matrixStack, buffer, packedLight, packedOverlay);
        stick5.render(matrixStack, buffer, packedLight, packedOverlay);
        stick6.render(matrixStack, buffer, packedLight, packedOverlay);
        stick7.render(matrixStack, buffer, packedLight, packedOverlay);
        stick8.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
