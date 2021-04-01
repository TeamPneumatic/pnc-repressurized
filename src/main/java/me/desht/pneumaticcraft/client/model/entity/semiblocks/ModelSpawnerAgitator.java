package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelSpawnerAgitator extends EntityModel<EntityHeatFrame> {
    //fields
    private final ModelRenderer top1;
    private final ModelRenderer top2;
    private final ModelRenderer top3;
    private final ModelRenderer top4;
    private final ModelRenderer side1;
    private final ModelRenderer side2;
    private final ModelRenderer side3;
    private final ModelRenderer side4;
    private final ModelRenderer bottom1;
    private final ModelRenderer bottom2;
    private final ModelRenderer bottom3;
    private final ModelRenderer bottom4;

    public ModelSpawnerAgitator() {
        textureWidth = 64;
        textureHeight = 64;

        top1 = new ModelRenderer(this);
        top1.setRotationPoint(-8.5F, 7.5F, 4.5F);
        top1.setTextureOffset(0, 0).addBox(0.0F, 0.0F, 1.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        top2 = new ModelRenderer(this);
        top2.setRotationPoint(-8.5F, 7.5F, -8.5F);
        top2.setTextureOffset(0, 6).addBox(0.0F, 0.0F, 0.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        top3 = new ModelRenderer(this);
        top3.setRotationPoint(4.5F, 7.5F, 4.5F);
        top3.setTextureOffset(0, 24).addBox(1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        top4 = new ModelRenderer(this);
        top4.setRotationPoint(-7.5F, 7.5F, 4.5F);
        top4.setTextureOffset(0, 38).addBox(-1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        side1 = new ModelRenderer(this);
        side1.setRotationPoint(-8.5F, 11.5F, -8.5F);
        side1.setTextureOffset(28, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side1.setTextureOffset(32, 52).addBox(2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side1.setTextureOffset(24, 52).addBox(0.0F, -1.0F, 2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side2 = new ModelRenderer(this);
        side2.setRotationPoint(7.5F, 11.5F, -8.5F);
        side2.setTextureOffset(40, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side2.setTextureOffset(44, 52).addBox(0.0F, -1.0F, 2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side2.setTextureOffset(36, 52).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side3 = new ModelRenderer(this);
        side3.setRotationPoint(-8.5F, 11.5F, 7.5F);
        side3.setTextureOffset(16, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side3.setTextureOffset(12, 52).addBox(2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side3.setTextureOffset(20, 52).addBox(0.0F, -1.0F, -2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side4 = new ModelRenderer(this);
        side4.setRotationPoint(7.5F, 11.5F, 7.5F);
        side4.setTextureOffset(4, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side4.setTextureOffset(8, 52).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side4.setTextureOffset(0, 52).addBox(0.0F, -1.0F, -2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        bottom1 = new ModelRenderer(this);
        bottom1.setRotationPoint(-8.5F, 21.5F, 4.5F);
        bottom1.setTextureOffset(0, 12).addBox(0.0F, 0.0F, 1.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        bottom2 = new ModelRenderer(this);
        bottom2.setRotationPoint(-7.5F, 21.5F, 4.5F);
        bottom2.setTextureOffset(28, 38).addBox(-1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        bottom3 = new ModelRenderer(this);
        bottom3.setRotationPoint(4.5F, 21.5F, 4.5F);
        bottom3.setTextureOffset(28, 24).addBox(1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        bottom4 = new ModelRenderer(this);
        bottom4.setRotationPoint(-8.5F, 21.5F, -8.5F);
        bottom4.setTextureOffset(0, 18).addBox(0.0F, 0.0F, 0.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        top1.render(matrixStack, buffer, packedLight, packedOverlay);
        top2.render(matrixStack, buffer, packedLight, packedOverlay);
        top3.render(matrixStack, buffer, packedLight, packedOverlay);
        top4.render(matrixStack, buffer, packedLight, packedOverlay);
        side1.render(matrixStack, buffer, packedLight, packedOverlay);
        side2.render(matrixStack, buffer, packedLight, packedOverlay);
        side3.render(matrixStack, buffer, packedLight, packedOverlay);
        side4.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom1.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom2.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom3.render(matrixStack, buffer, packedLight, packedOverlay);
        bottom4.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void setRotationAngles(EntityHeatFrame entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
