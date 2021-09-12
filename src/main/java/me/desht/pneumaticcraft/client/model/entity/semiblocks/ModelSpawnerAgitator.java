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
        texWidth = 64;
        texHeight = 64;

        top1 = new ModelRenderer(this);
        top1.setPos(-8.5F, 7.5F, 4.5F);
        top1.texOffs(0, 0).addBox(0.0F, 0.0F, 1.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        top2 = new ModelRenderer(this);
        top2.setPos(-8.5F, 7.5F, -8.5F);
        top2.texOffs(0, 6).addBox(0.0F, 0.0F, 0.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        top3 = new ModelRenderer(this);
        top3.setPos(4.5F, 7.5F, 4.5F);
        top3.texOffs(0, 24).addBox(1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        top4 = new ModelRenderer(this);
        top4.setPos(-7.5F, 7.5F, 4.5F);
        top4.texOffs(0, 38).addBox(-1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        side1 = new ModelRenderer(this);
        side1.setPos(-8.5F, 11.5F, -8.5F);
        side1.texOffs(28, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side1.texOffs(32, 52).addBox(2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side1.texOffs(24, 52).addBox(0.0F, -1.0F, 2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side2 = new ModelRenderer(this);
        side2.setPos(7.5F, 11.5F, -8.5F);
        side2.texOffs(40, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side2.texOffs(44, 52).addBox(0.0F, -1.0F, 2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side2.texOffs(36, 52).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side3 = new ModelRenderer(this);
        side3.setPos(-8.5F, 11.5F, 7.5F);
        side3.texOffs(16, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side3.texOffs(12, 52).addBox(2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side3.texOffs(20, 52).addBox(0.0F, -1.0F, -2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        side4 = new ModelRenderer(this);
        side4.setPos(7.5F, 11.5F, 7.5F);
        side4.texOffs(4, 52).addBox(0.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side4.texOffs(8, 52).addBox(-2.0F, -1.0F, 0.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);
        side4.texOffs(0, 52).addBox(0.0F, -1.0F, -2.0F, 1.0F, 11.0F, 1.0F, 0.0F, true);

        bottom1 = new ModelRenderer(this);
        bottom1.setPos(-8.5F, 21.5F, 4.5F);
        bottom1.texOffs(0, 12).addBox(0.0F, 0.0F, 1.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);

        bottom2 = new ModelRenderer(this);
        bottom2.setPos(-7.5F, 21.5F, 4.5F);
        bottom2.texOffs(28, 38).addBox(-1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        bottom3 = new ModelRenderer(this);
        bottom3.setPos(4.5F, 21.5F, 4.5F);
        bottom3.texOffs(28, 24).addBox(1.0F, 0.0F, -10.0F, 3.0F, 3.0F, 11.0F, 0.0F, true);

        bottom4 = new ModelRenderer(this);
        bottom4.setPos(-8.5F, 21.5F, -8.5F);
        bottom4.texOffs(0, 18).addBox(0.0F, 0.0F, 0.0F, 17.0F, 3.0F, 3.0F, 0.0F, true);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
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
    public void setupAnim(EntityHeatFrame entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
