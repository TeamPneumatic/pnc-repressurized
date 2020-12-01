package me.desht.pneumaticcraft.client.model.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelTransferGadget extends EntityModel<EntityTransferGadget> {
    private final ModelRenderer mainPart;

    public ModelTransferGadget() {
        textureWidth = 32;
        textureHeight = 32;

        mainPart = new ModelRenderer(this);
        mainPart.setRotationPoint(0.0F, 12.0F, 0.0F);
        mainPart.setTextureOffset(0, 0).addBox(-0.5F, 6.0F, -6.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
    }

    @Override
    public void setRotationAngles(EntityTransferGadget entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        mainPart.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
