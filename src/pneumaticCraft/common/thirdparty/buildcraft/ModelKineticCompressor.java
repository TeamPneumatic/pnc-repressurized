package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelKineticCompressor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4Move;
    ModelRenderer Shape5Move;
    ModelRenderer Shape6;

    public ModelKineticCompressor(){
        textureWidth = 128;
        textureHeight = 128;

        Shape1 = new ModelRenderer(this, 36, 0);
        Shape1.addBox(0F, 0F, 0F, 16, 16, 2);
        Shape1.setRotationPoint(-8F, 8F, -8F);
        Shape1.setTextureSize(128, 128);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 16, 16, 2);
        Shape2.setRotationPoint(-8F, 8F, 6F);
        Shape2.setTextureSize(128, 128);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 18);
        Shape3.addBox(0F, 0F, 0F, 10, 10, 12);
        Shape3.setRotationPoint(-5F, 11F, -6F);
        Shape3.setTextureSize(128, 128);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4Move = new ModelRenderer(this, 0, 54);
        Shape4Move.addBox(-6F, -6F, 0F, 12, 12, 2);
        Shape4Move.setRotationPoint(0F, 16F, -6F);
        Shape4Move.setTextureSize(128, 128);
        Shape4Move.mirror = true;
        setRotation(Shape4Move, 0F, 0F, 0F);
        Shape5Move = new ModelRenderer(this, 0, 40);
        Shape5Move.addBox(-6F, -6F, 0F, 12, 12, 2);
        Shape5Move.setRotationPoint(0F, 16F, 4F);
        Shape5Move.setTextureSize(128, 128);
        Shape5Move.mirror = true;
        setRotation(Shape5Move, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 72, 0);
        Shape6.addBox(0F, 0F, 0F, 16, 16, 2);
        Shape6.setRotationPoint(-8F, 8F, -1F);
        Shape6.setTextureSize(128, 128);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4Move.render(f5);
        Shape5Move.render(f5);
        Shape6.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4Move.render(size);
        Shape5Move.render(size);
        Shape6.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_KINETIC_COMPRESSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }
}
