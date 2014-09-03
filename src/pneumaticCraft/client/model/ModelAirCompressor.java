package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ModelAirCompressor extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Output1;
    ModelRenderer Output2;
    ModelRenderer Output3;
    ModelRenderer Output4;
    ModelRenderer Bottom;
    ModelRenderer Front;
    ModelRenderer Back;
    ModelRenderer Left;
    ModelRenderer Right;
    ModelRenderer Top2;
    ModelRenderer Top1;

    public ModelAirCompressor(){
        textureWidth = 64;
        textureHeight = 64;

        Output1 = new ModelRenderer(this, 34, 0);
        Output1.addBox(0F, 0F, 0F, 2, 1, 3);
        Output1.setRotationPoint(-1F, 14F, 5F);
        Output1.setTextureSize(64, 32);
        Output1.mirror = true;
        setRotation(Output1, 0F, 0F, 0F);
        Output2 = new ModelRenderer(this, 34, 0);
        Output2.addBox(0F, 0F, 0F, 2, 1, 3);
        Output2.setRotationPoint(-1F, 17F, 5F);
        Output2.setTextureSize(64, 32);
        Output2.mirror = true;
        setRotation(Output2, 0F, 0F, 0F);
        Output3 = new ModelRenderer(this, 34, 4);
        Output3.addBox(0F, 0F, 0F, 1, 2, 3);
        Output3.setRotationPoint(1F, 15F, 5F);
        Output3.setTextureSize(64, 32);
        Output3.mirror = true;
        setRotation(Output3, 0F, 0F, 0F);
        Output4 = new ModelRenderer(this, 34, 4);
        Output4.addBox(0F, 0F, 0F, 1, 2, 3);
        Output4.setRotationPoint(-2F, 15F, 5F);
        Output4.setTextureSize(64, 32);
        Output4.mirror = true;
        setRotation(Output4, 0F, 0F, 0F);
        Bottom = new ModelRenderer(this, 0, 23);
        Bottom.addBox(0F, 0F, 0F, 8, 1, 8);
        Bottom.setRotationPoint(-4F, 23F, -4F);
        Bottom.setTextureSize(64, 32);
        Bottom.mirror = true;
        setRotation(Bottom, 0F, 0F, 0F);
        Front = new ModelRenderer(this, 46, 18);
        Front.addBox(0F, 0F, 0F, 8, 13, 1);
        Front.setRotationPoint(-4F, 11F, -5F);
        Front.setTextureSize(64, 32);
        Front.mirror = true;
        setRotation(Front, 0F, 0F, 0F);
        Back = new ModelRenderer(this, 0, 0);
        Back.addBox(0F, 0F, 0F, 8, 13, 1);
        Back.setRotationPoint(-4F, 11F, 4F);
        Back.setTextureSize(64, 32);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);
        Left = new ModelRenderer(this, 0, 0);
        Left.addBox(0F, 0F, 0F, 1, 13, 8);
        Left.setRotationPoint(4F, 11F, -4F);
        Left.setTextureSize(64, 32);
        Left.mirror = true;
        setRotation(Left, 0F, 0F, 0F);
        Right = new ModelRenderer(this, 0, 0);
        Right.addBox(0F, 0F, 0F, 1, 13, 8);
        Right.setRotationPoint(-5F, 11F, -4F);
        Right.setTextureSize(64, 32);
        Right.mirror = true;
        setRotation(Right, 0F, 0F, 0F);
        Top2 = new ModelRenderer(this, 18, 9);
        Top2.addBox(0F, 0F, 0F, 8, 1, 8);
        Top2.setRotationPoint(-4F, 10F, -4F);
        Top2.setTextureSize(64, 32);
        Top2.mirror = true;
        setRotation(Top2, 0F, 0F, 0F);
        Top1 = new ModelRenderer(this, 44, 0);
        Top1.addBox(0F, 0F, 0F, 5, 1, 5);
        Top1.setRotationPoint(-2.5F, 9F, -2.5F);
        Top1.setTextureSize(64, 32);
        Top1.mirror = true;
        setRotation(Top1, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Output1.render(f5);
        Output2.render(f5);
        Output3.render(f5);
        Output4.render(f5);
        Bottom.render(f5);
        Front.render(f5);
        Back.render(f5);
        Left.render(f5);
        Right.render(f5);
        Top2.render(f5);
        Top1.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        Output1.render(size);
        Output2.render(size);
        Output3.render(size);
        Output4.render(size);
        Bottom.render(size);
        Front.render(size);
        Back.render(size);
        Left.render(size);
        Right.render(size);
        Top2.render(size);
        Top1.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(){
        return Textures.MODEL_AIR_COMPRESSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

}
