package me.desht.pneumaticcraft.client.model.semiblocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCropSupport extends ModelBase {
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;

    public ModelCropSupport() {
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 16);
        Shape1.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape1.setRotationPoint(-8.5F, 11.5F, -8.5F);
        Shape1.setTextureSize(64, 64);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 4, 16);
        Shape2.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape2.setRotationPoint(7.5F, 11.5F, -8.5F);
        Shape2.setTextureSize(64, 64);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 16);
        Shape3.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape3.setRotationPoint(-8.5F, 11.5F, 7.5F);
        Shape3.setTextureSize(64, 64);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 16);
        Shape4.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape4.setRotationPoint(7.5F, 11.5F, 7.5F);
        Shape4.setTextureSize(64, 64);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
