package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ModelVortexCannon extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;

    public ModelVortexCannon(){
        textureWidth = 64;
        textureHeight = 32;

        Shape1 = new ModelRenderer(this, 0, 18);
        Shape1.addBox(0F, 0F, 0F, 16, 7, 7);
        Shape1.setRotationPoint(-8F, 10F, -4F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 12);
        Shape2.addBox(0F, 0F, 0F, 16, 5, 1);
        Shape2.setRotationPoint(-8F, 11F, -5F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 12);
        Shape3.addBox(0F, 0F, 0F, 16, 5, 1);
        Shape3.setRotationPoint(-8F, 11F, 3F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 6);
        Shape4.addBox(0F, 0F, 0F, 16, 1, 5);
        Shape4.setRotationPoint(-8F, 9F, -3F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 6);
        Shape5.addBox(0F, 0F, 0F, 16, 1, 5);
        Shape5.setRotationPoint(-8F, 17F, -3F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 46, 0);
        Shape6.addBox(0F, 0F, 0F, 7, 1, 2);
        Shape6.setRotationPoint(-2F, 18F, -1.5F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 0);
        Shape7.addBox(0F, 0F, 0F, 2, 3, 2);
        Shape7.setRotationPoint(-2F, 19F, -1.5F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 56, 3);
        Shape8.addBox(0F, 0F, 0F, 2, 5, 2);
        Shape8.setRotationPoint(2F, 18F, -1.5F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, -0.3665191F);
        Shape9 = new ModelRenderer(this, 8, 0);
        Shape9.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape9.setRotationPoint(-7F, 8F, -1F);
        Shape9.setTextureSize(64, 32);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
        Shape10 = new ModelRenderer(this, 8, 0);
        Shape10.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape10.setRotationPoint(6F, 8F, 0F);
        Shape10.setTextureSize(64, 32);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);
        Shape11 = new ModelRenderer(this, 8, 0);
        Shape11.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape11.setRotationPoint(6F, 8F, -2F);
        Shape11.setTextureSize(64, 32);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
        Shape5.render(f5);
        Shape6.render(f5);
        Shape7.render(f5);
        Shape8.render(f5);
        Shape9.render(f5);
        Shape10.render(f5);
        Shape11.render(f5);
    }

    public void renderModel(float size){
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        Shape9.render(size);
        Shape10.render(size);
        Shape11.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        renderModel(size);
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_VORTEX_CANNON;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }

}
