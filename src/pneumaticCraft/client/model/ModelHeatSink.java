package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityCompressedIronBlock;
import pneumaticCraft.lib.Textures;

public class ModelHeatSink extends ModelBase implements IBaseModel{
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
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer Shape12;

    public ModelHeatSink(){
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 16, 1, 16);
        Shape1.setRotationPoint(-8F, 23F, -8F);
        Shape1.setTextureSize(64, 64);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 17);
        Shape2.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape2.setRotationPoint(7F, 16F, -8F);
        Shape2.setTextureSize(64, 64);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 17);
        Shape3.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape3.setRotationPoint(-5F, 16F, -8F);
        Shape3.setTextureSize(64, 64);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 17);
        Shape4.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape4.setRotationPoint(-8F, 16F, -8F);
        Shape4.setTextureSize(64, 64);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 17);
        Shape5.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape5.setRotationPoint(-2F, 16F, -8F);
        Shape5.setTextureSize(64, 64);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 0, 17);
        Shape6.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape6.setRotationPoint(1F, 16F, -8F);
        Shape6.setTextureSize(64, 64);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 17);
        Shape7.addBox(0F, 0F, 0F, 1, 7, 16);
        Shape7.setRotationPoint(4F, 16F, -8F);
        Shape7.setTextureSize(64, 64);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 0, 49);
        Shape8.addBox(0F, 0F, 0F, 14, 5, 2);
        Shape8.setRotationPoint(-7F, 18F, -1F);
        Shape8.setTextureSize(64, 64);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Shape9 = new ModelRenderer(this, 0, 40);
        Shape9.addBox(0F, 0F, 0F, 14, 3, 1);
        Shape9.setRotationPoint(-7F, 20F, -7F);
        Shape9.setTextureSize(64, 64);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
        Shape10 = new ModelRenderer(this, 0, 40);
        Shape10.addBox(0F, 0F, 0F, 14, 3, 1);
        Shape10.setRotationPoint(-7F, 20F, 6F);
        Shape10.setTextureSize(64, 64);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);
        Shape11 = new ModelRenderer(this, 0, 44);
        Shape11.addBox(0F, 0F, 0F, 14, 4, 1);
        Shape11.setRotationPoint(-7F, 19F, 3F);
        Shape11.setTextureSize(64, 64);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F);
        Shape12 = new ModelRenderer(this, 0, 44);
        Shape12.addBox(0F, 0F, 0F, 14, 4, 1);
        Shape12.setRotationPoint(-7F, 19F, -4F);
        Shape12.setTextureSize(64, 64);
        Shape12.mirror = true;
        setRotation(Shape12, 0F, 0F, 0F);
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
        Shape10.render(f5);
        Shape11.render(f5);
        Shape12.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){

        if(te != null) {
            int heatLevel = ((TileEntityCompressedIronBlock)te).getHeatLevel();
            double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
            GL11.glColor4d(color[0], color[1], color[2], 1);
            GL11.glTranslated(0.5, 0.5, 0);
            GL11.glRotated(90, 1, 0, 0);
            GL11.glTranslated(-0.5, -1, -0.5);
        }
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        Shape9.render(size);
        Shape10.render(size);
        Shape11.render(size);
        Shape12.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_HEAT_SINK;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }
}
