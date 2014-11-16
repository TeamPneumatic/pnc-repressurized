package pneumaticCraft.client.model.tubemodules;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelCharging extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;

    public ModelCharging(){
        textureWidth = 64;
        textureHeight = 32;

        Shape1 = new ModelRenderer(this, 22, 0);
        Shape1.addBox(0F, 0F, 0F, 2, 2, 2);
        Shape1.setRotationPoint(1F, 15F, 8F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 3.141593F, 0F);
        Shape2 = new ModelRenderer(this, 12, 0);
        Shape2.addBox(0F, 0F, 0F, 3, 3, 2);
        Shape2.setRotationPoint(1.5F, 14.5F, 6F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 3.141593F, 0F);
        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, 0F, 0F, 4, 4, 2);
        Shape3.setRotationPoint(2F, 14F, 4F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 3.141593F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
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
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){}

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_CHARGING_MODULE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
