package pneumaticCraft.client.model.tubemodules;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelAirGrate extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer top;
    ModelRenderer side1;
    ModelRenderer side2;
    ModelRenderer side3;
    ModelRenderer side4;
    ModelRenderer base1;
    ModelRenderer base2;
    ModelRenderer base3;

    public ModelAirGrate(){
        textureWidth = 128;
        textureHeight = 128;

        top = new ModelRenderer(this, 42, 19);
        top.addBox(0F, 0F, 0F, 14, 0, 14);
        top.setRotationPoint(-7F, 9F, 8F);
        top.setTextureSize(128, 128);
        top.mirror = true;
        setRotation(top, -1.570796F, 0F, 0F);
        side1 = new ModelRenderer(this, 0, 18);
        side1.addBox(0F, 0F, 0F, 16, 1, 1);
        side1.setRotationPoint(-8F, 23F, 7F);
        side1.setTextureSize(128, 128);
        side1.mirror = true;
        setRotation(side1, 0F, 0F, 0F);
        side2 = new ModelRenderer(this, 0, 21);
        side2.addBox(0F, 0F, 0F, 16, 1, 1);
        side2.setRotationPoint(-8F, 8F, 7F);
        side2.setTextureSize(128, 128);
        side2.mirror = true;
        setRotation(side2, 0F, 0F, 0F);
        side3 = new ModelRenderer(this, 50, 0);
        side3.addBox(0F, 0F, 0F, 1, 1, 14);
        side3.setRotationPoint(-8F, 23F, 7F);
        side3.setTextureSize(128, 128);
        side3.mirror = true;
        setRotation(side3, 1.570796F, 0F, 0F);
        side4 = new ModelRenderer(this, 82, 0);
        side4.addBox(0F, 0F, 0F, 1, 1, 14);
        side4.setRotationPoint(7F, 23F, 7F);
        side4.setTextureSize(128, 128);
        side4.mirror = true;
        setRotation(side4, 1.570796F, 0F, 0F);
        base1 = new ModelRenderer(this, 69, 0);
        base1.addBox(0F, 0F, 0F, 6, 2, 6);
        base1.setRotationPoint(-3F, 13F, 4F);
        base1.setTextureSize(128, 128);
        base1.mirror = true;
        setRotation(base1, -1.570796F, 0F, 0F);
        base2 = new ModelRenderer(this, 0, 25);
        base2.addBox(0F, 0F, 0F, 12, 2, 12);
        base2.setRotationPoint(-6F, 10F, 6F);
        base2.setTextureSize(128, 128);
        base2.mirror = true;
        setRotation(base2, -1.570796F, 0F, 0F);
        base3 = new ModelRenderer(this, 0, 0);
        base3.addBox(2F, 0F, 0F, 16, 1, 16);
        base3.setRotationPoint(-10F, 8F, 7F);
        base3.setTextureSize(128, 128);
        base3.mirror = true;
        setRotation(base3, -1.570796F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        top.render(f5);
        side1.render(f5);
        side2.render(f5);
        side3.render(f5);
        side4.render(f5);
        base1.render(f5);
        base2.render(f5);
        base3.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        top.render(size);
        side1.render(size);
        side2.render(size);
        side3.render(size);
        side4.render(size);
        base1.render(size);
        base2.render(size);
        base3.render(size);
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_AIR_GRATE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

}
