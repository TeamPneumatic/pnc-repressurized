package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.util.ForgeDirection;

public class ModelPressureTube extends ModelBase{
    //fields
    ModelRenderer Left1;
    ModelRenderer Left2;
    ModelRenderer Left3;
    ModelRenderer Left4;
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
    ModelRenderer Base11;
    ModelRenderer Base12;
    ModelRenderer CapLeft;

    public ModelPressureTube(){
        textureWidth = 64;
        textureHeight = 32;

        Left1 = new ModelRenderer(this, 0, 10);
        Left1.addBox(2F, -2F, -1F, 6, 1, 2);
        Left1.setRotationPoint(0F, 16F, 0F);
        Left1.setTextureSize(64, 32);
        Left1.mirror = true;
        setRotation(Left1, 0F, 0F, 0F);
        Left2 = new ModelRenderer(this, 0, 10);
        Left2.addBox(2F, 1F, -1F, 6, 1, 2);
        Left2.setRotationPoint(0F, 16F, 0F);
        Left2.setTextureSize(64, 32);
        Left2.mirror = true;
        setRotation(Left2, 0F, 0F, 0F);
        Left3 = new ModelRenderer(this, 0, 6);
        Left3.addBox(2F, -1F, -2F, 6, 2, 1);
        Left3.setRotationPoint(0F, 16F, 0F);
        Left3.setTextureSize(64, 32);
        Left3.mirror = true;
        setRotation(Left3, 0F, 0F, 0F);
        Left4 = new ModelRenderer(this, 0, 6);
        Left4.addBox(2F, -1F, 1F, 6, 2, 1);
        Left4.setRotationPoint(0F, 16F, 0F);
        Left4.setTextureSize(64, 32);
        Left4.mirror = true;
        setRotation(Left4, 0F, 0F, 0F);
        Base1 = new ModelRenderer(this, 0, 0);
        Base1.addBox(0F, 0F, 0F, 1, 4, 1);
        Base1.setRotationPoint(1F, 14F, -2F);
        Base1.setTextureSize(64, 32);
        Base1.mirror = true;
        setRotation(Base1, 0F, 0F, 0F);
        Base2 = new ModelRenderer(this, 0, 0);
        Base2.addBox(0F, 0F, 0F, 1, 4, 1);
        Base2.setRotationPoint(1F, 14F, 1F);
        Base2.setTextureSize(64, 32);
        Base2.mirror = true;
        setRotation(Base2, 0F, 0F, 0F);
        Base3 = new ModelRenderer(this, 0, 0);
        Base3.addBox(0F, 0F, 0F, 1, 4, 1);
        Base3.setRotationPoint(-2F, 14F, 1F);
        Base3.setTextureSize(64, 32);
        Base3.mirror = true;
        setRotation(Base3, 0F, 0F, 0F);
        Base4 = new ModelRenderer(this, 0, 0);
        Base4.addBox(0F, 0F, 0F, 1, 4, 1);
        Base4.setRotationPoint(-2F, 14F, -2F);
        Base4.setTextureSize(64, 32);
        Base4.mirror = true;
        setRotation(Base4, 0F, 0F, 0F);
        Base5 = new ModelRenderer(this, 0, 0);
        Base5.addBox(0F, 0F, 0F, 1, 1, 2);
        Base5.setRotationPoint(1F, 14F, -1F);
        Base5.setTextureSize(64, 32);
        Base5.mirror = true;
        setRotation(Base5, 0F, 0F, 0F);
        Base6 = new ModelRenderer(this, 0, 0);
        Base6.addBox(0F, 0F, 0F, 1, 1, 2);
        Base6.setRotationPoint(-2F, 14F, -1F);
        Base6.setTextureSize(64, 32);
        Base6.mirror = true;
        setRotation(Base6, 0F, 0F, 0F);
        Base7 = new ModelRenderer(this, 0, 0);
        Base7.addBox(0F, 0F, 0F, 1, 1, 2);
        Base7.setRotationPoint(1F, 17F, -1F);
        Base7.setTextureSize(64, 32);
        Base7.mirror = true;
        setRotation(Base7, 0F, 0F, 0F);
        Base8 = new ModelRenderer(this, 0, 0);
        Base8.addBox(0F, 0F, 0F, 1, 1, 2);
        Base8.setRotationPoint(-2F, 17F, -1F);
        Base8.setTextureSize(64, 32);
        Base8.mirror = true;
        setRotation(Base8, 0F, 0F, 0F);
        Base9 = new ModelRenderer(this, 0, 0);
        Base9.addBox(0F, 0F, 0F, 2, 1, 1);
        Base9.setRotationPoint(-1F, 14F, 1F);
        Base9.setTextureSize(64, 32);
        Base9.mirror = true;
        setRotation(Base9, 0F, 0F, 0F);
        Base10 = new ModelRenderer(this, 0, 0);
        Base10.addBox(0F, 0F, 0F, 2, 1, 1);
        Base10.setRotationPoint(-1F, 17F, 1F);
        Base10.setTextureSize(64, 32);
        Base10.mirror = true;
        setRotation(Base10, 0F, 0F, 0F);
        Base11 = new ModelRenderer(this, 0, 0);
        Base11.addBox(0F, 0F, 0F, 2, 1, 1);
        Base11.setRotationPoint(-1F, 14F, -2F);
        Base11.setTextureSize(64, 32);
        Base11.mirror = true;
        setRotation(Base11, 0F, 0F, 0F);
        Base12 = new ModelRenderer(this, 0, 0);
        Base12.addBox(0F, 0F, 0F, 2, 1, 1);
        Base12.setRotationPoint(-1F, 17F, -2F);
        Base12.setTextureSize(64, 32);
        Base12.mirror = true;
        setRotation(Base12, 0F, 0F, 0F);
        CapLeft = new ModelRenderer(this, 17, 0);
        CapLeft.addBox(2F, -1F, -1F, 1, 2, 2);
        CapLeft.setRotationPoint(0F, 16F, 0F);
        CapLeft.setTextureSize(64, 32);
        CapLeft.mirror = true;
        setRotation(CapLeft, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Left1.render(f5);
        Left2.render(f5);
        Left3.render(f5);
        Left4.render(f5);
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
        Base11.render(f5);
        Base12.render(f5);
        CapLeft.render(f5);
    }

    public void renderModel(float size, boolean[] sidesConnected){
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
        Base11.render(size);
        Base12.render(size);
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            switch(dir){
                case UP:
                    setTubeRotation(0, 0, -90);
                    break;
                case DOWN:
                    setTubeRotation(0, 0, 90);
                    break;
                case NORTH:
                    setTubeRotation(0, -90, 0);
                    break;
                case SOUTH:
                    setTubeRotation(0, 90, 0);
                    break;
                case WEST:
                    setTubeRotation(0, 0, 180);
                    break;
                case EAST:
                    setTubeRotation(0, 0, 0);
                    break;
            }
            if(sidesConnected[dir.ordinal()]) {
                Left1.render(size);
                Left2.render(size);
                Left3.render(size);
                Left4.render(size);
            } else {
                CapLeft.render(size);
            }

        }
    }

    private void setTubeRotation(float x, float y, float z){
        x = (float)Math.toRadians(x);
        y = (float)Math.toRadians(y);
        z = (float)Math.toRadians(z);
        setRotation(Left1, x, y, z);
        setRotation(Left2, x, y, z);
        setRotation(Left3, x, y, z);
        setRotation(Left4, x, y, z);
        setRotation(CapLeft, x, y, z);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
