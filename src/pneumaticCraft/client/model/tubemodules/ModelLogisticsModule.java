package pneumaticCraft.client.model.tubemodules;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelLogisticsModule extends ModelBase implements IBaseModel{
    //fields
    public ModelRenderer base1;
    ModelRenderer base2;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    public ModelRenderer notPowered, powered, action, notEnoughAir;

    public ModelLogisticsModule(){
        textureWidth = 128;
        textureHeight = 128;

        notPowered = new ModelRenderer(this, 72, 0);
        notPowered.addBox(0F, 0F, 0F, 6, 2, 6);
        notPowered.setRotationPoint(-3F, 13F, 4F);
        notPowered.setTextureSize(128, 128);
        notPowered.mirror = true;
        setRotation(notPowered, -1.570796F, 0F, 0F);
        powered = new ModelRenderer(this, 48, 0);
        powered.addBox(0F, 0F, 0F, 6, 2, 6);
        powered.setRotationPoint(-3F, 13F, 4F);
        powered.setTextureSize(128, 128);
        powered.mirror = true;
        setRotation(powered, -1.570796F, 0F, 0F);
        action = new ModelRenderer(this, 24, 0);
        action.addBox(0F, 0F, 0F, 6, 2, 6);
        action.setRotationPoint(-3F, 13F, 4F);
        action.setTextureSize(128, 128);
        action.mirror = true;
        setRotation(action, -1.570796F, 0F, 0F);
        notEnoughAir = new ModelRenderer(this, 0, 0);
        notEnoughAir.addBox(0F, 0F, 0F, 6, 2, 6);
        notEnoughAir.setRotationPoint(-3F, 13F, 4F);
        notEnoughAir.setTextureSize(128, 128);
        notEnoughAir.mirror = true;
        setRotation(notEnoughAir, -1.570796F, 0F, 0F);

        base2 = new ModelRenderer(this, 0, 25);
        base2.addBox(0F, 0F, 0F, 12, 2, 12);
        base2.setRotationPoint(-6F, 10F, 6F);
        base2.setTextureSize(128, 128);
        base2.mirror = true;
        setRotation(base2, -1.570796F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 0, 39);
        Shape1.addBox(0F, 0F, 0F, 1, 13, 1);
        Shape1.setRotationPoint(5.5F, 9.5F, 5.5F);
        Shape1.setTextureSize(128, 128);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 4, 39);
        Shape2.addBox(0F, 0F, 0F, 1, 13, 1);
        Shape2.setRotationPoint(-6.5F, 9.5F, 5.5F);
        Shape2.setTextureSize(128, 128);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 8, 39);
        Shape3.addBox(0F, 0F, 0F, 11, 1, 1);
        Shape3.setRotationPoint(-5.5F, 9.5F, 5.5F);
        Shape3.setTextureSize(128, 128);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 8, 41);
        Shape4.addBox(0F, 0F, 0F, 11, 1, 1);
        Shape4.setRotationPoint(-5.5F, 21.5F, 5.5F);
        Shape4.setTextureSize(128, 128);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        base1.render(f5);
        base2.render(f5);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        base1.render(size);
        base2.render(size);

    }

    public void renderChannelColorFrame(float size){
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_LOGISTICS;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }
}
