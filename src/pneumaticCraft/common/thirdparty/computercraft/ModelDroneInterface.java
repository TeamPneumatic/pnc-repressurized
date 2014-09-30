package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelDroneInterface extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer Neck;
    ModelRenderer Dish_Base_1;
    ModelRenderer Dish_Base_2;
    ModelRenderer Dish_Long_1;
    ModelRenderer Dish_Long_2;
    ModelRenderer Arm;
    ModelRenderer End;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;

    public ModelDroneInterface(){
        textureWidth = 64;
        textureHeight = 64;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 16, 2, 16);
        Base.setRotationPoint(-8F, 22F, -8F);
        Base.setTextureSize(64, 64);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Neck = new ModelRenderer(this, 0, 18);
        Neck.addBox(0F, 0F, 1F, 2, 10, 1);
        Neck.setRotationPoint(-1F, 12F, -1F);
        Neck.setTextureSize(64, 64);
        Neck.mirror = true;
        setRotation(Neck, 0F, 0F, 0F);
        Dish_Base_1 = new ModelRenderer(this, 26, 25);
        Dish_Base_1.addBox(-3F, -4F, 0F, 6, 1, 1);
        Dish_Base_1.setRotationPoint(0F, 12F, 0F);
        Dish_Base_1.setTextureSize(64, 64);
        Dish_Base_1.mirror = true;
        setRotation(Dish_Base_1, -0.7330383F, -0.002272F, 0F);
        Dish_Base_2 = new ModelRenderer(this, 34, 18);
        Dish_Base_2.addBox(-5F, -2F, 0F, 1, 6, 1);
        Dish_Base_2.setRotationPoint(0F, 12F, 0F);
        Dish_Base_2.setTextureSize(64, 64);
        Dish_Base_2.mirror = true;
        setRotation(Dish_Base_2, -0.7330383F, 0F, 0F);
        Dish_Long_1 = new ModelRenderer(this, 8, 18);
        Dish_Long_1.addBox(-6F, -1F, 0F, 1, 4, 1);
        Dish_Long_1.setRotationPoint(0F, 12F, 0F);
        Dish_Long_1.setTextureSize(64, 64);
        Dish_Long_1.mirror = true;
        setRotation(Dish_Long_1, -0.7330383F, 0F, 0F);
        Dish_Long_2 = new ModelRenderer(this, 40, 25);
        Dish_Long_2.addBox(-2F, -5F, 0F, 4, 1, 1);
        Dish_Long_2.setRotationPoint(0F, 12F, 0F);
        Dish_Long_2.setTextureSize(64, 64);
        Dish_Long_2.mirror = true;
        setRotation(Dish_Long_2, -0.7330383F, 0F, 0F);
        Arm = new ModelRenderer(this, 50, 25);
        Arm.addBox(-1F, 0F, -5F, 2, 1, 5);
        Arm.setRotationPoint(0F, 12F, 0F);
        Arm.setTextureSize(64, 64);
        Arm.mirror = true;
        setRotation(Arm, -0.7853982F, 0F, 0F);
        End = new ModelRenderer(this, 56, 18);
        End.addBox(-1F, -1F, -5F, 2, 1, 1);
        End.setRotationPoint(0F, 12F, 0F);
        End.setTextureSize(64, 64);
        End.mirror = true;
        setRotation(End, -0.7958701F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 8, 23);
        Shape1.addBox(-4F, -3F, 0F, 8, 8, 1);
        Shape1.setRotationPoint(0F, 12F, 0F);
        Shape1.setTextureSize(64, 64);
        Shape1.mirror = true;
        setRotation(Shape1, -0.7330383F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 32);
        Shape2.addBox(-3F, 5F, 0F, 6, 1, 1);
        Shape2.setRotationPoint(0F, 12F, 0F);
        Shape2.setTextureSize(64, 64);
        Shape2.mirror = true;
        setRotation(Shape2, -0.7330383F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 34);
        Shape3.addBox(-2F, 6F, 0F, 4, 1, 1);
        Shape3.setRotationPoint(0F, 12F, 0F);
        Shape3.setTextureSize(64, 64);
        Shape3.mirror = true;
        setRotation(Shape3, -0.7330383F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 36);
        Shape4.addBox(4F, -2F, 0F, 1, 6, 1);
        Shape4.setRotationPoint(0F, 12F, 0F);
        Shape4.setTextureSize(64, 64);
        Shape4.mirror = true;
        setRotation(Shape4, -0.7330383F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 4, 37);
        Shape5.addBox(5F, -1F, 0F, 1, 4, 1);
        Shape5.setRotationPoint(0F, 12F, 0F);
        Shape5.setTextureSize(64, 64);
        Shape5.mirror = true;
        setRotation(Shape5, -0.7330383F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        Neck.render(f5);
        Dish_Base_1.render(f5);
        Dish_Base_2.render(f5);
        Dish_Long_1.render(f5);
        Dish_Long_2.render(f5);
        Arm.render(f5);
        End.render(f5);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
        Shape5.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity){
        Dish_Base_1.rotateAngleY = par2;
        Dish_Base_2.rotateAngleY = par2;
        Dish_Long_1.rotateAngleY = par2;
        Dish_Long_2.rotateAngleY = par2;
        Arm.rotateAngleY = par2;
        End.rotateAngleY = par2;
        Shape1.rotateAngleY = par2;
        Shape2.rotateAngleY = par2;
        Shape3.rotateAngleY = par2;
        Shape4.rotateAngleY = par2;
        Shape5.rotateAngleY = par2;
        Neck.rotateAngleY = 0;

        Dish_Base_1.rotateAngleX = par1;
        Dish_Base_2.rotateAngleX = par1;
        Dish_Long_1.rotateAngleX = par1;
        Dish_Long_2.rotateAngleX = par1;
        Arm.rotateAngleX = par1;
        End.rotateAngleX = par1;
        Shape1.rotateAngleX = par1;
        Shape2.rotateAngleX = par1;
        Shape3.rotateAngleX = par1;
        Shape4.rotateAngleX = par1;
        Shape5.rotateAngleX = par1;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_DRONE_INTERFACE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity tile, float partialTicks){
        if(tile instanceof TileEntityDroneInterface) {
            TileEntityDroneInterface inter = (TileEntityDroneInterface)tile;
            render(null, inter.rotationPitch, inter.rotationYaw, 0, 0, 0, 1 / 16F);
        } else {
            render(null, (float)Math.toRadians(-42), 0, 0, 0, 0, 1 / 16F);
        }
    }
}
