package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelHeatFrame extends EntityModel<EntityHeatFrame> {
    //fields
    private final ModelRenderer bottom;
    private final ModelRenderer side1;
    private final ModelRenderer side2;
    private final ModelRenderer side3;
    private final ModelRenderer side4;
    private final ModelRenderer topCorner1;
    private final ModelRenderer topCorner2;
    private final ModelRenderer topCorner3;
    private final ModelRenderer topCorner4;
    private final ModelRenderer top1;
    private final ModelRenderer top2;
    private final ModelRenderer top3;
    private final ModelRenderer top4;
    private final ModelRenderer top5;
    private final ModelRenderer top6;
    private final ModelRenderer top7;
    private final ModelRenderer top8;
    private final ModelRenderer top9;
    private final ModelRenderer top10;
    private final ModelRenderer top11;
    private final ModelRenderer top12;

    public ModelHeatFrame() {
        textureWidth = 128;
        textureHeight = 32;

        bottom = new ModelRenderer(this);
        bottom.setRotationPoint(4.5F, 20.5F, -8.5F);
        bottom.setTextureOffset(0, 0).addBox(-13.0F, -1.0F, 0.0F, 17.0F, 5.0F, 17.0F, 0.0F, true);

        side1 = new ModelRenderer(this);
        side1.setRotationPoint(-8.5F, 11.5F, -8.5F);
        side1.setTextureOffset(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side2 = new ModelRenderer(this);
        side2.setRotationPoint(7.5F, 11.5F, -8.5F);
        side2.setTextureOffset(4, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side3 = new ModelRenderer(this);
        side3.setRotationPoint(-8.5F, 11.5F, 7.5F);
        side3.setTextureOffset(12, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side4 = new ModelRenderer(this);
        side4.setRotationPoint(7.5F, 11.5F, 7.5F);
        side4.setTextureOffset(8, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        topCorner1 = new ModelRenderer(this);
        topCorner1.setRotationPoint(-8.5F, 7.5F, 4.5F);
        topCorner1.setTextureOffset(48, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner2 = new ModelRenderer(this);
        topCorner2.setRotationPoint(4.5F, 7.5F, -8.5F);
        topCorner2.setTextureOffset(0, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner3 = new ModelRenderer(this);
        topCorner3.setRotationPoint(-8.5F, 7.5F, -8.5F);
        topCorner3.setTextureOffset(16, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner4 = new ModelRenderer(this);
        topCorner4.setRotationPoint(4.5F, 7.5F, 4.5F);
        topCorner4.setTextureOffset(32, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        top1 = new ModelRenderer(this);
        top1.setRotationPoint(-4.5F, 7.5F, 7.5F);
        top1.setTextureOffset(64, 26).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 1.0F, 0.0F, true);

        top2 = new ModelRenderer(this);
        top2.setRotationPoint(-4.5F, 7.5F, -8.5F);
        top2.setTextureOffset(64, 29).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 1.0F, 0.0F, true);

        top3 = new ModelRenderer(this);
        top3.setRotationPoint(7.5F, 7.5F, -4.5F);
        top3.setTextureOffset(51, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        top4 = new ModelRenderer(this);
        top4.setRotationPoint(-8.5F, 7.5F, -4.5F);
        top4.setTextureOffset(71, 10).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        top5 = new ModelRenderer(this);
        top5.setRotationPoint(-8.5F, 9.5F, -4.5F);
        top5.setTextureOffset(91, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top6 = new ModelRenderer(this);
        top6.setRotationPoint(7.5F, 9.5F, -4.5F);
        top6.setTextureOffset(71, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top7 = new ModelRenderer(this);
        top7.setRotationPoint(-4.5F, 9.5F, -8.5F);
        top7.setTextureOffset(84, 30).addBox(0.0F, 0.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top8 = new ModelRenderer(this);
        top8.setRotationPoint(-4.5F, 9.5F, 7.5F);
        top8.setTextureOffset(84, 28).addBox(0.0F, 0.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top9 = new ModelRenderer(this);
        top9.setRotationPoint(-8.5F, 9.5F, -4.5F);
        top9.setTextureOffset(91, 10).addBox(0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top10 = new ModelRenderer(this);
        top10.setRotationPoint(7.5F, 9.5F, -4.5F);
        top10.setTextureOffset(102, 5).addBox(0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top11 = new ModelRenderer(this);
        top11.setRotationPoint(-4.5F, 9.5F, 7.5F);
        top11.setTextureOffset(84, 26).addBox(0.0F, 1.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top12 = new ModelRenderer(this);
        top12.setRotationPoint(-4.5F, 9.5F, -8.5F);
        top12.setTextureOffset(84, 24).addBox(0.0F, 1.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bottom.render(matrixStack, buffer, packedLight, packedOverlay);
        side1.render(matrixStack, buffer, packedLight, packedOverlay);
        side2.render(matrixStack, buffer, packedLight, packedOverlay);
        side3.render(matrixStack, buffer, packedLight, packedOverlay);
        side4.render(matrixStack, buffer, packedLight, packedOverlay);
        topCorner1.render(matrixStack, buffer, packedLight, packedOverlay);
        topCorner2.render(matrixStack, buffer, packedLight, packedOverlay);
        topCorner3.render(matrixStack, buffer, packedLight, packedOverlay);
        topCorner4.render(matrixStack, buffer, packedLight, packedOverlay);
        top1.render(matrixStack, buffer, packedLight, packedOverlay);
        top2.render(matrixStack, buffer, packedLight, packedOverlay);
        top3.render(matrixStack, buffer, packedLight, packedOverlay);
        top4.render(matrixStack, buffer, packedLight, packedOverlay);
        top5.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        top6.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        top7.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        top8.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        top9.render(matrixStack, buffer, packedLight, packedOverlay);
        top10.render(matrixStack, buffer, packedLight, packedOverlay);
        top11.render(matrixStack, buffer, packedLight, packedOverlay);
        top12.render(matrixStack, buffer, packedLight, packedOverlay);
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
