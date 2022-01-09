package me.desht.pneumaticcraft.client.model.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelTransferGadget extends EntityModel<EntityTransferGadget> {
    private final ModelRenderer inPart1;
    private final ModelRenderer inPart2;
    private final ModelRenderer betweenPart;
    private final ModelRenderer outPart1;
    private final ModelRenderer outPart2;

    public ModelTransferGadget() {
        texWidth = 64;
        texHeight = 16;

        inPart1 = new ModelRenderer(this);
        inPart1.setPos(2.0F, 12.0F, 0.0F);
        inPart1.texOffs(32, 0).addBox(-1.5F, 2.0F, -4.0F, 1.0F, 8.0F, 8.0F, 0.0F, false);

        inPart2 = new ModelRenderer(this);
        inPart2.setPos(-2.0F, 12.0F, 0.0F);
        inPart2.texOffs(30, 0).addBox(0.5F, 4.0F, -2.0F, 1.0F, 4.0F, 4.0F, 0.0F, false);

        betweenPart = new ModelRenderer(this);
        betweenPart.setPos(0.0F, 12.0F, 0.0F);
        betweenPart.texOffs(18, 4).addBox(-0.5F, 3.0F, -3.0F, 1.0F, 6.0F, 6.0F, 0.0F, false);

        outPart1 = new ModelRenderer(this);
        outPart1.setPos(-2.0F, 12.0F, 0.0F);
        outPart1.texOffs(0, 0).addBox(0.5F, 2.0F, -4.0F, 1.0F, 8.0F, 8.0F, 0.0F, false);

        outPart2 = new ModelRenderer(this);
        outPart2.setPos(0.0F, 12.0F, 0.0F);
        outPart2.texOffs(10, 0).addBox(0.5F, 4.0F, -2.0F, 1.0F, 4.0F, 4.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(EntityTransferGadget entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        inPart1.render(matrixStack, buffer, packedLight, packedOverlay);
        inPart2.render(matrixStack, buffer, packedLight, packedOverlay);
        betweenPart.render(matrixStack, buffer, packedLight, packedOverlay);
        outPart1.render(matrixStack, buffer, packedLight, packedOverlay);
        outPart2.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
