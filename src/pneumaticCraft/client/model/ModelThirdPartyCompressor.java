package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ModelThirdPartyCompressor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer TopRF;
    ModelRenderer TopEU;
    ModelRenderer TopMJ;
    ModelRenderer top1;
    ModelRenderer top2;
    ModelRenderer cyl1;
    ModelRenderer cyl2;
    ModelRenderer cyl3;
    ModelRenderer cyl4;
    ModelRenderer center;
    ModelRenderer hor3;
    ModelRenderer hor2;
    ModelRenderer hor1;
    ModelRenderer hor4;
    ModelRenderer vert1;
    ModelRenderer vert2;
    ModelRenderer vert4;
    ModelRenderer vert3;
    ModelRenderer bottom1;
    ModelRenderer bottom2;
    private final Type type;

    public enum Type{
        RF, EU, MJ;
    }

    public ModelThirdPartyCompressor(Type type){
        this.type = type;
        textureWidth = 128;
        textureHeight = 128;

        TopRF = new ModelRenderer(this, 0, 73);
        TopRF.addBox(0F, 0F, 0F, 14, 2, 16);
        TopRF.setRotationPoint(-7F, 8F, -8F);
        TopRF.setTextureSize(128, 128);
        TopRF.mirror = true;
        setRotation(TopRF, 0F, 0F, 0F);
        TopEU = new ModelRenderer(this, 0, 0);
        TopEU.addBox(0F, 0F, 0F, 14, 2, 16);
        TopEU.setRotationPoint(-7F, 8F, -8F);
        TopEU.setTextureSize(128, 128);
        TopEU.mirror = true;
        setRotation(TopEU, 0F, 0F, 0F);
        TopMJ = new ModelRenderer(this, 0, 52);
        TopMJ.addBox(0F, 0F, 0F, 14, 2, 16);
        TopMJ.setRotationPoint(-7F, 8F, -8F);
        TopMJ.setTextureSize(128, 128);
        TopMJ.mirror = true;
        setRotation(TopMJ, 0F, 0F, 0F);
        top1 = new ModelRenderer(this, 47, 4);
        top1.addBox(0F, 0F, 0F, 3, 2, 16);
        top1.setRotationPoint(-7F, 10F, -8F);
        top1.setTextureSize(128, 128);
        top1.mirror = true;
        setRotation(top1, 0F, 0F, 0F);
        top2 = new ModelRenderer(this, 47, 4);
        top2.addBox(0F, 0F, 0F, 3, 2, 16);
        top2.setRotationPoint(4F, 10F, -8F);
        top2.setTextureSize(128, 128);
        top2.mirror = true;
        setRotation(top2, 0F, 0F, 0F);
        cyl1 = new ModelRenderer(this, 90, 0);
        cyl1.addBox(-2.5F, -6F, 0F, 5, 12, 14);
        cyl1.setRotationPoint(0F, 16F, -7F);
        cyl1.setTextureSize(128, 128);
        cyl1.mirror = true;
        setRotation(cyl1, 0F, 0F, 1.570796F);
        cyl2 = new ModelRenderer(this, 90, 0);
        cyl2.addBox(-2.5F, -6F, 0F, 5, 12, 14);
        cyl2.setRotationPoint(0F, 16F, -7F);
        cyl2.setTextureSize(128, 128);
        cyl2.mirror = true;
        setRotation(cyl2, 0F, 0F, -0.7853982F);
        cyl3 = new ModelRenderer(this, 90, 0);
        cyl3.addBox(-2.5F, -6F, 0F, 5, 12, 14);
        cyl3.setRotationPoint(0F, 16F, -7F);
        cyl3.setTextureSize(128, 128);
        cyl3.mirror = true;
        setRotation(cyl3, 0F, 0F, 0F);
        cyl4 = new ModelRenderer(this, 90, 0);
        cyl4.addBox(-2.5F, -6F, 0F, 5, 12, 14);
        cyl4.setRotationPoint(0F, 16F, -7F);
        cyl4.setTextureSize(128, 128);
        cyl4.mirror = true;
        setRotation(cyl4, 0F, 0F, 0.7853982F);
        center = new ModelRenderer(this, 0, 26);
        center.addBox(0F, 0F, 0F, 8, 8, 16);
        center.setRotationPoint(-4F, 12F, -8F);
        center.setTextureSize(128, 128);
        center.mirror = true;
        setRotation(center, 0F, 0F, 0F);
        hor3 = new ModelRenderer(this, 35, 24);
        hor3.addBox(0F, 0F, 0F, 1, 1, 14);
        hor3.setRotationPoint(7F, 8F, -7F);
        hor3.setTextureSize(128, 128);
        hor3.mirror = true;
        setRotation(hor3, 0F, 0F, 0F);
        hor2 = new ModelRenderer(this, 35, 24);
        hor2.addBox(0F, 0F, 0F, 1, 1, 14);
        hor2.setRotationPoint(7F, 23F, -7F);
        hor2.setTextureSize(128, 128);
        hor2.mirror = true;
        setRotation(hor2, 0F, 0F, 0F);
        hor1 = new ModelRenderer(this, 35, 24);
        hor1.addBox(0F, 0F, 0F, 1, 1, 14);
        hor1.setRotationPoint(-8F, 23F, -7F);
        hor1.setTextureSize(128, 128);
        hor1.mirror = true;
        setRotation(hor1, 0F, 0F, 0F);
        hor4 = new ModelRenderer(this, 35, 24);
        hor4.addBox(0F, 0F, 0F, 1, 1, 14);
        hor4.setRotationPoint(-8F, 8F, -7F);
        hor4.setTextureSize(128, 128);
        hor4.mirror = true;
        setRotation(hor4, 0F, 0F, 0F);
        vert1 = new ModelRenderer(this, 72, 0);
        vert1.addBox(0F, 0F, 0F, 1, 14, 1);
        vert1.setRotationPoint(7F, 9F, -7F);
        vert1.setTextureSize(128, 128);
        vert1.mirror = true;
        setRotation(vert1, 0F, 0F, 0F);
        vert2 = new ModelRenderer(this, 72, 0);
        vert2.addBox(0F, 0F, 0F, 1, 14, 1);
        vert2.setRotationPoint(-8F, 9F, -7F);
        vert2.setTextureSize(128, 128);
        vert2.mirror = true;
        setRotation(vert2, 0F, 0F, 0F);
        vert4 = new ModelRenderer(this, 72, 0);
        vert4.addBox(0F, 0F, 0F, 1, 14, 1);
        vert4.setRotationPoint(7F, 9F, 6F);
        vert4.setTextureSize(128, 128);
        vert4.mirror = true;
        setRotation(vert4, 0F, 0F, 0F);
        vert3 = new ModelRenderer(this, 72, 0);
        vert3.addBox(0F, 0F, 0F, 1, 14, 1);
        vert3.setRotationPoint(-8F, 9F, 6F);
        vert3.setTextureSize(128, 128);
        vert3.mirror = true;
        setRotation(vert3, 0F, 0F, 0F);
        bottom1 = new ModelRenderer(this, 0, 19);
        bottom1.addBox(0F, 0F, 0F, 16, 1, 2);
        bottom1.setRotationPoint(-8F, 22F, -5F);
        bottom1.setTextureSize(128, 128);
        bottom1.mirror = true;
        setRotation(bottom1, 0F, 0F, 0F);
        bottom2 = new ModelRenderer(this, 0, 19);
        bottom2.addBox(0F, 0F, 0F, 16, 1, 2);
        bottom2.setRotationPoint(-8F, 22F, 3F);
        bottom2.setTextureSize(128, 128);
        bottom2.mirror = true;
        setRotation(bottom2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        if(type == Type.RF) TopRF.render(f5);
        else if(type == Type.EU) TopEU.render(f5);
        else if(type == Type.MJ) TopMJ.render(f5);
        top1.render(f5);
        top2.render(f5);
        cyl1.render(f5);
        cyl2.render(f5);
        cyl3.render(f5);
        cyl4.render(f5);
        center.render(f5);
        hor3.render(f5);
        hor2.render(f5);
        hor1.render(f5);
        hor4.render(f5);
        vert1.render(f5);
        vert2.render(f5);
        vert4.render(f5);
        vert3.render(f5);
        bottom1.render(f5);
        bottom2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        if(type == Type.RF) TopRF.render(size);
        else if(type == Type.EU) TopEU.render(size);
        else if(type == Type.MJ) TopMJ.render(size);
        top1.render(size);
        top2.render(size);
        cyl1.render(size);
        cyl2.render(size);
        cyl3.render(size);
        cyl4.render(size);
        center.render(size);
        hor3.render(size);
        hor2.render(size);
        hor1.render(size);
        hor4.render(size);
        vert1.render(size);
        vert2.render(size);
        vert4.render(size);
        vert3.render(size);
        bottom1.render(size);
        bottom2.render(size);
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_THIRD_PARTY_COMPRESSOR;
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
