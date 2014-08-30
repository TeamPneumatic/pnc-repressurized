package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.lib.Textures;

public class ModelUniversalSensor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base1;
    ModelRenderer Base2;
    ModelRenderer Base3;
    ModelRenderer Base4;
    ModelRenderer Base5;
    ModelRenderer Base6;
    ModelRenderer Base7;
    ModelRenderer Base8;
    ModelRenderer Base9;
    ModelRenderer Base10;
    ModelRenderer TubeConnection1;
    ModelRenderer TubeConnection2;
    ModelRenderer DishBase;
    ModelRenderer DishLeg;
    ModelRenderer Dish1;
    ModelRenderer Dish2;
    ModelRenderer Dish3;

    public ModelUniversalSensor(){
        textureWidth = 64;
        textureHeight = 64;

        Base1 = new ModelRenderer(this, 0, 0);
        Base1.addBox(0F, 0F, 0F, 16, 1, 16);
        Base1.setRotationPoint(-8F, 23F, -8F);
        Base1.setTextureSize(64, 64);
        Base1.mirror = true;
        setRotation(Base1, 0F, 0F, 0F);
        Base2 = new ModelRenderer(this, 0, 0);
        Base2.addBox(0F, 0F, 0F, 15, 1, 15);
        Base2.setRotationPoint(-7.5F, 22F, -7.5F);
        Base2.setTextureSize(64, 64);
        Base2.mirror = true;
        setRotation(Base2, 0F, 0F, 0F);
        Base3 = new ModelRenderer(this, 0, 0);
        Base3.addBox(0F, 0F, 0F, 14, 1, 14);
        Base3.setRotationPoint(-7F, 21F, -7F);
        Base3.setTextureSize(64, 64);
        Base3.mirror = true;
        setRotation(Base3, 0F, 0F, 0F);
        Base4 = new ModelRenderer(this, 0, 0);
        Base4.addBox(0F, 0F, 0F, 13, 1, 13);
        Base4.setRotationPoint(-6.5F, 20F, -6.5F);
        Base4.setTextureSize(64, 64);
        Base4.mirror = true;
        setRotation(Base4, 0F, 0F, 0F);
        Base5 = new ModelRenderer(this, 0, 0);
        Base5.addBox(0F, 0F, 0F, 12, 1, 12);
        Base5.setRotationPoint(-6F, 19F, -6F);
        Base5.setTextureSize(64, 64);
        Base5.mirror = true;
        setRotation(Base5, 0F, 0F, 0F);
        Base6 = new ModelRenderer(this, 0, 0);
        Base6.addBox(0F, 0F, 0F, 11, 1, 11);
        Base6.setRotationPoint(-5.5F, 18F, -5.5F);
        Base6.setTextureSize(64, 64);
        Base6.mirror = true;
        setRotation(Base6, 0F, 0F, 0F);
        Base7 = new ModelRenderer(this, 0, 0);
        Base7.addBox(0F, 0F, 0F, 10, 1, 10);
        Base7.setRotationPoint(-5F, 17F, -5F);
        Base7.setTextureSize(64, 64);
        Base7.mirror = true;
        setRotation(Base7, 0F, 0F, 0F);
        Base8 = new ModelRenderer(this, 0, 0);
        Base8.addBox(0F, 0F, 0F, 9, 1, 9);
        Base8.setRotationPoint(-4.5F, 16F, -4.5F);
        Base8.setTextureSize(64, 64);
        Base8.mirror = true;
        setRotation(Base8, 0F, 0F, 0F);
        Base9 = new ModelRenderer(this, 0, 0);
        Base9.addBox(0F, 0F, 0F, 8, 1, 8);
        Base9.setRotationPoint(-4F, 15F, -4F);
        Base9.setTextureSize(64, 64);
        Base9.mirror = true;
        setRotation(Base9, 0F, 0F, 0F);
        Base10 = new ModelRenderer(this, 0, 0);
        Base10.addBox(0F, 0F, 0F, 7, 1, 7);
        Base10.setRotationPoint(-3.5F, 14F, -3.5F);
        Base10.setTextureSize(64, 64);
        Base10.mirror = true;
        setRotation(Base10, 0F, 0F, 0F);
        TubeConnection1 = new ModelRenderer(this, 0, 39);
        TubeConnection1.addBox(0F, 0F, 0F, 4, 9, 16);
        TubeConnection1.setRotationPoint(-2F, 14.1F, -8F);
        TubeConnection1.setTextureSize(64, 64);
        TubeConnection1.mirror = true;
        setRotation(TubeConnection1, 0F, 0F, 0F);
        TubeConnection2 = new ModelRenderer(this, 0, 51);
        TubeConnection2.addBox(0F, 0F, 0F, 16, 9, 4);
        TubeConnection2.setRotationPoint(-8F, 14.1F, -2F);
        TubeConnection2.setTextureSize(64, 64);
        TubeConnection2.mirror = true;
        setRotation(TubeConnection2, 0F, 0F, 0F);
        DishBase = new ModelRenderer(this, 0, 26);
        DishBase.addBox(0F, 0F, 0F, 6, 1, 6);
        DishBase.setRotationPoint(-3F, 13F, -3F);
        DishBase.setTextureSize(64, 64);
        DishBase.mirror = true;
        setRotation(DishBase, 0F, 0F, 0F);
        DishLeg = new ModelRenderer(this, 0, 26);
        DishLeg.addBox(0F, 0F, 0F, 1, 3, 1);
        DishLeg.setRotationPoint(-0.5F, 10.5F, 1F);
        DishLeg.setTextureSize(64, 64);
        DishLeg.mirror = true;
        setRotation(DishLeg, -0.3665191F, 0F, 0F);
        Dish1 = new ModelRenderer(this, 0, 26);
        Dish1.addBox(0F, 0F, 0F, 4, 2, 1);
        Dish1.setRotationPoint(-2F, 9F, 1F);
        Dish1.setTextureSize(64, 64);
        Dish1.mirror = true;
        setRotation(Dish1, 0F, 0F, 0F);
        Dish2 = new ModelRenderer(this, 0, 26);
        Dish2.addBox(0F, 0F, 0F, 2, 2, 1);
        Dish2.setRotationPoint(-3.2F, 9F, 0F);
        Dish2.setTextureSize(64, 64);
        Dish2.mirror = true;
        setRotation(Dish2, 0F, -0.5235988F, 0F);
        Dish3 = new ModelRenderer(this, 0, 26);
        Dish3.addBox(0F, 0F, 0F, 2, 2, 1);
        Dish3.setRotationPoint(1.5F, 9F, 1F);
        Dish3.setTextureSize(64, 64);
        Dish3.mirror = true;
        setRotation(Dish3, 0F, 0.5235988F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base1.render(f5);
        Base2.render(f5);
        Base3.render(f5);
        Base4.render(f5);
        Base5.render(f5);
        Base6.render(f5);
        Base7.render(f5);
        Base8.render(f5);
        Base9.render(f5);
        Base10.render(f5);
        TubeConnection1.render(f5);
        TubeConnection2.render(f5);
        DishBase.render(f5);
        DishLeg.render(f5);
        Dish1.render(f5);
        Dish2.render(f5);
        Dish3.render(f5);
    }

    public void renderModel(float size, float dishRotation){
        Base1.render(size);
        Base2.render(size);
        Base3.render(size);
        Base4.render(size);
        Base5.render(size);
        Base6.render(size);
        Base7.render(size);
        Base8.render(size);
        Base9.render(size);
        Base10.render(size);
        TubeConnection1.render(size);
        TubeConnection2.render(size);
        GL11.glPushMatrix();
        GL11.glRotatef(dishRotation, 0, 1, 0);
        DishBase.render(size);
        DishLeg.render(size);
        Dish1.render(size);
        Dish2.render(size);
        Dish3.render(size);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderModel(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityUniversalSensor) {
            TileEntityUniversalSensor tile = (TileEntityUniversalSensor)te;
            renderModel(size, tile.oldDishRotation + (tile.dishRotation - tile.oldDishRotation) * partialTicks);
            tile.renderRangeLines();
        } else {
            renderModel(size, 0);
        }
    }

    @Override
    public ResourceLocation getModelTexture(){
        return Textures.MODEL_UNIVERSAL_SENSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
