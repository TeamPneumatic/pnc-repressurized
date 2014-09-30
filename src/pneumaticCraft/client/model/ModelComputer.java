package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

public class ModelComputer extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Table;
    ModelRenderer Screen;
    private final ResourceLocation texture;

    public ModelComputer(ResourceLocation texture){
        this.texture = texture;
        textureWidth = 64;
        textureHeight = 32;

        Leg1 = new ModelRenderer(this, 0, 0);
        Leg1.addBox(0F, 0F, 0F, 2, 8, 2);
        Leg1.setRotationPoint(-7F, 16F, -7F);
        Leg1.setTextureSize(64, 32);
        Leg1.mirror = true;
        setRotation(Leg1, 0F, 0F, 0F);
        Leg2 = new ModelRenderer(this, 0, 0);
        Leg2.addBox(0F, 0F, 0F, 2, 8, 2);
        Leg2.setRotationPoint(5F, 16F, -7F);
        Leg2.setTextureSize(64, 32);
        Leg2.mirror = true;
        setRotation(Leg2, 0F, 0F, 0F);
        Leg3 = new ModelRenderer(this, 0, 0);
        Leg3.addBox(0F, 0F, 0F, 2, 8, 2);
        Leg3.setRotationPoint(-7F, 16F, 5F);
        Leg3.setTextureSize(64, 32);
        Leg3.mirror = true;
        setRotation(Leg3, 0F, 0F, 0F);
        Leg4 = new ModelRenderer(this, 0, 0);
        Leg4.addBox(0F, 0F, 0F, 2, 8, 2);
        Leg4.setRotationPoint(5F, 16F, 5F);
        Leg4.setTextureSize(64, 32);
        Leg4.mirror = true;
        setRotation(Leg4, 0F, 0F, 0F);
        Table = new ModelRenderer(this, 0, 0);
        Table.addBox(0F, 0F, 0F, 14, 3, 14);
        Table.setRotationPoint(-7F, 13F, -7F);
        Table.setTextureSize(64, 32);
        Table.mirror = true;
        setRotation(Table, 0F, 0F, 0F);
        Screen = new ModelRenderer(this, 0, 18);
        Screen.addBox(0F, 0F, 0F, 12, 3, 11);
        Screen.setRotationPoint(-6F, 13F, -6F);
        Screen.setTextureSize(64, 32);
        Screen.mirror = true;
        setRotation(Screen, 0.2617994F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Leg1.render(f5);
        Leg2.render(f5);
        Leg3.render(f5);
        Leg4.render(f5);
        Table.render(f5);
        Screen.render(f5);
    }

    public void renderModel(float size){
        Leg1.render(size);
        Leg2.render(size);
        Leg3.render(size);
        Leg4.render(size);
        Table.render(size);
        Screen.render(size);
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
        return texture;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntitySecurityStation) ((TileEntitySecurityStation)te).renderRangeLines();
    }

}
