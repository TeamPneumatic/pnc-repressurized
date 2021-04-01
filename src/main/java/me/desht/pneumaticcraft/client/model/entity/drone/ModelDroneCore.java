package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDroneCore extends EntityModel<EntityDroneBase> {
//    private final ModelRenderer Base2;
//    private final ModelRenderer Base3;
//    private final ModelRenderer Base4;
//    private final ModelRenderer Base5;

    private final ModelRenderer done;
    private final ModelRenderer body;

    public ModelDroneCore() {
//        textureWidth = 64;
//        textureHeight = 32;
//
//        Base2 = new ModelRenderer(64, 32, 0, 12);
//        Base2.addBox(0F, 0F, 0F, 4, 4, 1);
//        Base2.setRotationPoint(-2F, 15F, -4F);
//        Base2.mirror = true;
//        Base3 = new ModelRenderer(64, 32, 0, 12);
//        Base3.addBox(0F, 0F, 0F, 4, 4, 1);
//        Base3.setRotationPoint(-2F, 15F, 3F);
//        Base3.mirror = true;
//        Base4 = new ModelRenderer(64, 32, 10, 12);
//        Base4.addBox(0F, 0F, 0F, 1, 4, 4);
//        Base4.setRotationPoint(3F, 15F, -2F);
//        Base4.mirror = true;
//        Base5 = new ModelRenderer(64, 32, 10, 12);
//        Base5.addBox(0F, 0F, 0F, 1, 4, 4);
//        Base5.setRotationPoint(-4F, 15F, -2F);
//        Base5.mirror = true;
        textureWidth = 128;
        textureHeight = 128;

        done = new ModelRenderer(this);
        done.setRotationPoint(0.0F, 22.5F, 0.0F);

        body = new ModelRenderer(this);
        body.setRotationPoint(0.0F, -3.0F, 0.0F);
        done.addChild(body);
        body.setTextureOffset(48, 106).addBox(-3.5F, -5.0F, -4.5F, 7.0F, 6.0F, 16.0F, 0.0F, false);
    }

    @Override
    public void setRotationAngles(EntityDroneBase drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        done.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
