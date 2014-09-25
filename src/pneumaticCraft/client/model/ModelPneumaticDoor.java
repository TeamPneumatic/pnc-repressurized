package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityPneumaticDoor;
import pneumaticCraft.lib.Textures;

public class ModelPneumaticDoor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;

    public ModelPneumaticDoor(){
        textureWidth = 64;
        textureHeight = 32;

        Shape1 = new ModelRenderer(this, 0, 24);
        Shape1.addBox(0F, 0F, 0F, 16, 3, 3);
        Shape1.setRotationPoint(-8F, -8F, -8F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 38, 0);
        Shape2.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape2.setRotationPoint(-8F, -5F, -8F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 50, 0);
        Shape3.addBox(0F, 0F, 0F, 2, 3, 3);
        Shape3.setRotationPoint(-1F, -5F, -8F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 38, 6);
        Shape4.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape4.setRotationPoint(5F, -5F, -8F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 24);
        Shape5.addBox(0F, 0F, 0F, 16, 2, 3);
        Shape5.setRotationPoint(-8F, -2F, -8F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 38, 12);
        Shape6.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape6.setRotationPoint(-8F, 0F, -8F);
        Shape6.setTextureSize(64, 32);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 50, 12);
        Shape7.addBox(0F, 0F, 0F, 2, 3, 3);
        Shape7.setRotationPoint(-1F, 0F, -8F);
        Shape7.setTextureSize(64, 32);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 38, 18);
        Shape8.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape8.setRotationPoint(5F, 0F, -8F);
        Shape8.setTextureSize(64, 32);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Shape9 = new ModelRenderer(this, 0, 0);
        Shape9.addBox(0F, 0F, 0F, 16, 21, 3);
        Shape9.setRotationPoint(-8F, 3F, -8F);
        Shape9.setTextureSize(64, 32);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
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
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PNEUMATIC_DOOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity tile, float partialTicks){
        if(tile instanceof TileEntityPneumaticDoor) {
            float rotation = ((TileEntityPneumaticDoor)tile).oldRotation + (((TileEntityPneumaticDoor)tile).rotation - ((TileEntityPneumaticDoor)tile).oldRotation) * partialTicks;
            boolean rightGoing = ((TileEntityPneumaticDoor)tile).rightGoing;
            GL11.glTranslatef((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
            GL11.glRotatef(rotation, 0, rightGoing ? -1 : 1, 0);
            GL11.glTranslatef((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);
            if(tile.getBlockMetadata() < 6) renderModel(size);
        } else {
            GL11.glTranslated(0, 0.5, 0);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            renderModel(size);
        }
    }

}
