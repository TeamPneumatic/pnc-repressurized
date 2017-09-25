package me.desht.pneumaticcraft.client.model.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelDoor extends ModelBase {
    private final ModelRenderer Shape1;
    private final ModelRenderer Shape2;
    private final ModelRenderer Shape3;
    private final ModelRenderer Shape4;
    private final ModelRenderer Shape5;
    private final ModelRenderer Shape6;
    private final ModelRenderer Shape7;
    private final ModelRenderer Shape8;
    private final ModelRenderer Shape9;

    public ModelDoor() {
        Shape1 = new ModelRenderer(this, 0, 24);
        Shape1.addBox(0F, 0F, 0F, 16, 3, 3);
        Shape1.setRotationPoint(-8F, -8F, -8F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 38, 0);
        Shape2.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape2.setRotationPoint(-8F, -5F, -8F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 50, 0);
        Shape3.addBox(0F, 0F, 0F, 2, 3, 3);
        Shape3.setRotationPoint(-1F, -5F, -8F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 38, 6);
        Shape4.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape4.setRotationPoint(5F, -5F, -8F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 24);
        Shape5.addBox(0F, 0F, 0F, 16, 2, 3);
        Shape5.setRotationPoint(-8F, -2F, -8F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 38, 12);
        Shape6.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape6.setRotationPoint(-8F, 0F, -8F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 50, 12);
        Shape7.addBox(0F, 0F, 0F, 2, 3, 3);
        Shape7.setRotationPoint(-1F, 0F, -8F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 38, 18);
        Shape8.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape8.setRotationPoint(5F, 0F, -8F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Shape9 = new ModelRenderer(this, 0, 0);
        Shape9.addBox(0F, 0F, 0F, 16, 21, 3);
        Shape9.setRotationPoint(-8F, 3F, -8F);
        Shape9.setTextureSize(64, 32);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public void renderModel(float size) {
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        Shape9.render(size);
    }
}
