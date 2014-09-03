package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ModelPressureChamberWindow extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Glass1;
    ModelRenderer NonGlass;

    public ModelPressureChamberWindow(){
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 13, 3, 16);
        Shape1.setRotationPoint(-5F, 21F, -8F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 3, 13, 16);
        Shape2.setRotationPoint(5F, 8F, -8F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, 0F, 0F, 13, 3, 16);
        Shape3.setRotationPoint(-8F, 8F, -8F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 0);
        Shape4.addBox(0F, 0F, 0F, 3, 13, 16);
        Shape4.setRotationPoint(-8F, 11F, -8F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 0);
        Shape5.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape5.setRotationPoint(-5F, 11F, -8F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 0, 0);
        Shape6.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape6.setRotationPoint(3F, 11F, -8F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 0);
        Shape7.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape7.setRotationPoint(-5F, 19F, -8F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 0, 0);
        Shape8.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape8.setRotationPoint(3F, 19F, -8F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Glass1 = new ModelRenderer(this, 0, 32);
        Glass1.addBox(0F, 0F, 0F, 10, 10, 15);
        Glass1.setRotationPoint(-5F, 11F, -7.5F);
        Glass1.setTextureSize(64, 32);
        Glass1.mirror = true;
        setRotation(Glass1, 0F, 0F, 0F);
        NonGlass = new ModelRenderer(this, 0, 0);
        NonGlass.addBox(0F, 0F, 0F, 10, 10, 15);
        NonGlass.setRotationPoint(-5F, 11F, -7.5F);
        NonGlass.setTextureSize(64, 32);
        NonGlass.mirror = true;
        setRotation(NonGlass, 0F, 0F, 0F);
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
        Glass1.render(f5);
    }

    public void renderModel(float size, boolean seeThrough){

        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);

        if(seeThrough) Glass1.render(size);
        else NonGlass.render(size);

    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        if(tile != null) {
            renderModel(size, tile.getBlockMetadata() >= 6);
        } else {
            renderModel(size, false);
        }
    }

    @Override
    public ResourceLocation getModelTexture(){
        return Textures.MODEL_AIR_COMPRESSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }

}
