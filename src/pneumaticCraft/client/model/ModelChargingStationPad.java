package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelChargingStationPad extends ModelBase{
    //fields
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer Shape12;
    ModelRenderer Shape13;
    ModelRenderer Shape14;
    ModelRenderer Shape15;
    ModelRenderer Shape16;

    public ModelChargingStationPad(){
        textureWidth = 64;
        textureHeight = 32;

        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape3.setRotationPoint(-5F, 17F, -4F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 3);
        Shape4.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape4.setRotationPoint(-4F, 17F, -5F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 9);
        Shape5.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape5.setRotationPoint(-5F, 17F, 3F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 0, 12);
        Shape6.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape6.setRotationPoint(-4F, 17F, 4F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 15);
        Shape7.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape7.setRotationPoint(3F, 17F, 4F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 0, 18);
        Shape8.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape8.setRotationPoint(4F, 17F, 3F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Shape9 = new ModelRenderer(this, 0, 21);
        Shape9.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape9.setRotationPoint(4F, 17F, -4F);
        Shape9.setTextureSize(64, 32);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
        Shape10 = new ModelRenderer(this, 0, 24);
        Shape10.addBox(0F, 0F, 0F, 1, 2, 1);
        Shape10.setRotationPoint(3F, 17F, -5F);
        Shape10.setTextureSize(64, 32);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);
        Shape11 = new ModelRenderer(this, 5, 0);
        Shape11.addBox(0F, 0F, 0F, 1, 1, 8);
        Shape11.setRotationPoint(-4F, 17F, -4F);
        Shape11.setTextureSize(64, 32);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F);
        Shape12 = new ModelRenderer(this, 5, 9);
        Shape12.addBox(0F, 0F, 0F, 1, 1, 8);
        Shape12.setRotationPoint(3F, 17F, -4F);
        Shape12.setTextureSize(64, 32);
        Shape12.mirror = true;
        setRotation(Shape12, 0F, 0F, 0F);
        Shape13 = new ModelRenderer(this, 23, 0);
        Shape13.addBox(0F, 0F, 0F, 1, 8, 1);
        Shape13.setRotationPoint(-4F, 9F, -4F);
        Shape13.setTextureSize(64, 32);
        Shape13.mirror = true;
        setRotation(Shape13, 0F, 0F, 0F);
        Shape14 = new ModelRenderer(this, 23, 9);
        Shape14.addBox(0F, 0F, 0F, 1, 8, 1);
        Shape14.setRotationPoint(-4F, 9F, 3F);
        Shape14.setTextureSize(64, 32);
        Shape14.mirror = true;
        setRotation(Shape14, 0F, 0F, 0F);
        Shape15 = new ModelRenderer(this, 27, 9);
        Shape15.addBox(0F, 0F, 0F, 1, 8, 1);
        Shape15.setRotationPoint(3F, 9F, 3F);
        Shape15.setTextureSize(64, 32);
        Shape15.mirror = true;
        setRotation(Shape15, 0F, 0F, 0F);
        Shape16 = new ModelRenderer(this, 27, 0);
        Shape16.addBox(0F, 0F, 0F, 1, 8, 1);
        Shape16.setRotationPoint(3F, 9F, -4F);
        Shape16.setTextureSize(64, 32);
        Shape16.mirror = true;
        setRotation(Shape16, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape3.render(f5);
        Shape4.render(f5);
        Shape5.render(f5);
        Shape6.render(f5);
        Shape7.render(f5);
        Shape8.render(f5);
        Shape9.render(f5);
        Shape10.render(f5);
        Shape11.render(f5);
        Shape12.render(f5);
        Shape13.render(f5);
        Shape14.render(f5);
        Shape15.render(f5);
        Shape16.render(f5);
    }

    public void renderModel(float size){
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        Shape9.render(size);
        Shape10.render(size);
        Shape11.render(size);
        Shape12.render(size);
        Shape13.render(size);
        Shape14.render(size);
        Shape15.render(size);
        Shape16.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
