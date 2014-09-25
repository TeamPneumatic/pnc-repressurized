package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ModelPneumaticCilinder extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Base;
    ModelRenderer Cilinder;
    ModelRenderer Input1;
    ModelRenderer Input2;

    public ModelPneumaticCilinder(){
        textureWidth = 64;
        textureHeight = 32;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 3, 9, 3);
        Base.setRotationPoint(-1.5F, 14F, -1.5F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Cilinder = new ModelRenderer(this, 0, 12);
        Cilinder.addBox(0F, 0F, 0F, 2, 5, 2);
        Cilinder.setRotationPoint(-1F, 9F, -1F);
        Cilinder.setTextureSize(64, 32);
        Cilinder.mirror = true;
        setRotation(Cilinder, 0F, 0F, 0F);
        Input1 = new ModelRenderer(this, 0, 19);
        Input1.addBox(0F, 0F, 0F, 1, 1, 1);
        Input1.setRotationPoint(-0.5F, 21F, -2.5F);
        Input1.setTextureSize(64, 32);
        Input1.mirror = true;
        setRotation(Input1, 0F, 0F, 0F);
        Input2 = new ModelRenderer(this, 0, 19);
        Input2.addBox(0F, 0F, 0F, 1, 1, 1);
        Input2.setRotationPoint(-0.5F, 15F, -2.5F);
        Input2.setTextureSize(64, 32);
        Input2.mirror = true;
        setRotation(Input2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        Cilinder.render(f5);
        Input1.render(f5);
        Input2.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        renderModel(size);
    }

    public void renderModel(float size){
        Base.render(size);
        Cilinder.render(size);
        Input1.render(size);
        Input2.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PNEUMATIC_CILINDER;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

}
