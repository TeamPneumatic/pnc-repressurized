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
        texWidth = 128;
        texHeight = 32;

        bottom = new ModelRenderer(this);
        bottom.setPos(4.5F, 20.5F, -8.5F);
        bottom.texOffs(0, 0).addBox(-13.0F, -1.0F, 0.0F, 17.0F, 5.0F, 17.0F, 0.0F, true);

        side1 = new ModelRenderer(this);
        side1.setPos(-8.5F, 11.5F, -8.5F);
        side1.texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side2 = new ModelRenderer(this);
        side2.setPos(7.5F, 11.5F, -8.5F);
        side2.texOffs(4, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side3 = new ModelRenderer(this);
        side3.setPos(-8.5F, 11.5F, 7.5F);
        side3.texOffs(12, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        side4 = new ModelRenderer(this);
        side4.setPos(7.5F, 11.5F, 7.5F);
        side4.texOffs(8, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 0.0F, true);

        topCorner1 = new ModelRenderer(this);
        topCorner1.setPos(-8.5F, 7.5F, 4.5F);
        topCorner1.texOffs(48, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner2 = new ModelRenderer(this);
        topCorner2.setPos(4.5F, 7.5F, -8.5F);
        topCorner2.texOffs(0, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner3 = new ModelRenderer(this);
        topCorner3.setPos(-8.5F, 7.5F, -8.5F);
        topCorner3.texOffs(16, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        topCorner4 = new ModelRenderer(this);
        topCorner4.setPos(4.5F, 7.5F, 4.5F);
        topCorner4.texOffs(32, 24).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, true);

        top1 = new ModelRenderer(this);
        top1.setPos(-4.5F, 7.5F, 7.5F);
        top1.texOffs(64, 26).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 1.0F, 0.0F, true);

        top2 = new ModelRenderer(this);
        top2.setPos(-4.5F, 7.5F, -8.5F);
        top2.texOffs(64, 29).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 1.0F, 0.0F, true);

        top3 = new ModelRenderer(this);
        top3.setPos(7.5F, 7.5F, -4.5F);
        top3.texOffs(51, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        top4 = new ModelRenderer(this);
        top4.setPos(-8.5F, 7.5F, -4.5F);
        top4.texOffs(71, 10).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        top5 = new ModelRenderer(this);
        top5.setPos(-8.5F, 9.5F, -4.5F);
        top5.texOffs(91, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top6 = new ModelRenderer(this);
        top6.setPos(7.5F, 9.5F, -4.5F);
        top6.texOffs(71, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top7 = new ModelRenderer(this);
        top7.setPos(-4.5F, 9.5F, -8.5F);
        top7.texOffs(84, 30).addBox(0.0F, 0.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top8 = new ModelRenderer(this);
        top8.setPos(-4.5F, 9.5F, 7.5F);
        top8.texOffs(84, 28).addBox(0.0F, 0.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top9 = new ModelRenderer(this);
        top9.setPos(-8.5F, 9.5F, -4.5F);
        top9.texOffs(91, 10).addBox(0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top10 = new ModelRenderer(this);
        top10.setPos(7.5F, 9.5F, -4.5F);
        top10.texOffs(102, 5).addBox(0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 9.0F, 0.0F, true);

        top11 = new ModelRenderer(this);
        top11.setPos(-4.5F, 9.5F, 7.5F);
        top11.texOffs(84, 26).addBox(0.0F, 1.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);

        top12 = new ModelRenderer(this);
        top12.setPos(-4.5F, 9.5F, -8.5F);
        top12.texOffs(84, 24).addBox(0.0F, 1.0F, 0.0F, 9.0F, 1.0F, 1.0F, 0.0F, true);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
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
        top5.render(matrixStack, buffer, packedLight, packedOverlay);
        top6.render(matrixStack, buffer, packedLight, packedOverlay);
        top7.render(matrixStack, buffer, packedLight, packedOverlay);
        top8.render(matrixStack, buffer, packedLight, packedOverlay);
        top9.render(matrixStack, buffer, packedLight, packedOverlay);
        top10.render(matrixStack, buffer, packedLight, packedOverlay);
        top11.render(matrixStack, buffer, packedLight, packedOverlay);
        top12.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void setupAnim(EntityHeatFrame entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
