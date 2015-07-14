package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.lib.Textures;

public class ModelUniversalSensor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base1;
    ModelRenderer Base2;
    ModelRenderer Dish1;
    ModelRenderer Dish2;
    ModelRenderer Dish3;
    ModelRenderer Dish4;
    ModelRenderer Dish5;
    ModelRenderer Dish6;
    private final ModelRenderer[] dishes;
    ModelRenderer TubeConnection;

    public ModelUniversalSensor(){
        textureWidth = 64;
        textureHeight = 64;

        Base1 = new ModelRenderer(this, 0, 0);
        Base1.addBox(0F, 0F, 0F, 16, 4, 16);
        Base1.setRotationPoint(-8F, 20F, -8F);
        Base1.setTextureSize(64, 64);
        Base1.mirror = true;
        setRotation(Base1, 0F, 0F, 0F);
        Base2 = new ModelRenderer(this, 0, 20);
        Base2.addBox(0F, 0F, 0F, 10, 3, 10);
        Base2.setRotationPoint(-5F, 17F, -5F);
        Base2.setTextureSize(64, 64);
        Base2.mirror = true;
        setRotation(Base2, 0F, 0F, 0F);
        Dish1 = new ModelRenderer(this, 0, 33);
        Dish1.addBox(-2F, 0F, -2F, 4, 1, 4);
        Dish1.setRotationPoint(0F, 16F, 0F);
        Dish1.setTextureSize(64, 64);
        Dish1.mirror = true;
        setRotation(Dish1, 0F, 0F, 0F);
        Dish2 = new ModelRenderer(this, 0, 38);
        Dish2.addBox(-3F, -1F, 0F, 1, 8, 4);
        Dish2.setRotationPoint(0F, 9F, -2F);
        Dish2.setTextureSize(64, 64);
        Dish2.mirror = true;
        setRotation(Dish2, 0F, 0F, -0.2268928F);
        Dish3 = new ModelRenderer(this, 0, 50);
        Dish3.addBox(-3.8F, 0F, 0.8F, 1, 4, 4);
        Dish3.setRotationPoint(0F, 8F, 0F);
        Dish3.setTextureSize(64, 64);
        Dish3.mirror = true;
        setRotation(Dish3, 0.0698132F, 0.3839724F, -0.2268928F);
        Dish4 = new ModelRenderer(this, 10, 50);
        Dish4.addBox(-3.8F, 0F, -4.7F, 1, 4, 4);
        Dish4.setRotationPoint(0F, 8F, 0F);
        Dish4.setTextureSize(64, 64);
        Dish4.mirror = true;
        setRotation(Dish4, -0.0698132F, -0.3839724F, -0.2268928F);
        Dish5 = new ModelRenderer(this, 0, 58);
        Dish5.addBox(-2F, 0F, -0.5F, 6, 1, 1);
        Dish5.setRotationPoint(0F, 12F, 0F);
        Dish5.setTextureSize(64, 64);
        Dish5.mirror = true;
        setRotation(Dish5, 0F, 0F, -0.2268928F);
        Dish6 = new ModelRenderer(this, 0, 60);
        Dish6.addBox(3F, 0F, -1F, 1, 1, 2);
        Dish6.setRotationPoint(0F, 10.2F, 0F);
        Dish6.setTextureSize(64, 64);
        Dish6.mirror = true;
        setRotation(Dish6, 0F, 0F, 0F);
        TubeConnection = new ModelRenderer(this, 16, 33);
        TubeConnection.addBox(-2F, 0F, -8F, 4, 6, 1);
        TubeConnection.setRotationPoint(0F, 14F, 0F);
        TubeConnection.setTextureSize(64, 64);
        TubeConnection.mirror = true;
        setRotation(TubeConnection, 0F, 0F, 0F);

        dishes = new ModelRenderer[]{Dish1, Dish2, Dish3, Dish4, Dish5, Dish6};
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base1.render(f5);
        Base2.render(f5);
        Dish1.render(f5);
        Dish2.render(f5);
        Dish3.render(f5);
        Dish4.render(f5);
        Dish5.render(f5);
        Dish6.render(f5);
        TubeConnection.render(f5);
    }

    public void renderModel(float size, float dishRotation, boolean[] sidesConnected){
        Base1.render(size);
        Base2.render(size);
        GL11.glPushMatrix();
        GL11.glRotatef(dishRotation, 0, 1, 0);
        Dish1.render(size);
        Dish2.render(size);
        Dish3.render(size);
        Dish4.render(size);
        Dish5.render(size);
        Dish6.render(size);
        GL11.glPopMatrix();
        ForgeDirection d = ForgeDirection.EAST;
        for(int i = 0; i < 4; i++) {
            d = d.getRotation(ForgeDirection.UP);
            if(sidesConnected[d.ordinal()]) {
                TubeConnection.rotateAngleY = (float)(i * Math.PI / 2);
                TubeConnection.render(size);
            }
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_UNIVERSAL_SENSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityUniversalSensor) {
            TileEntityUniversalSensor tile = (TileEntityUniversalSensor)te;
            renderModel(size, tile.oldDishRotation + (tile.dishRotation - tile.oldDishRotation) * partialTicks, tile.sidesConnected);
            tile.renderRangeLines();
        } else {
            renderModel(size, 0, new boolean[6]);
        }
    }

}
