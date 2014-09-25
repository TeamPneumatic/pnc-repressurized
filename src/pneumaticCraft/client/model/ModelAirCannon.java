package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.lib.Textures;

public class ModelAirCannon extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Input1;
    ModelRenderer Input2;
    ModelRenderer Input3;
    ModelRenderer Input4;
    ModelRenderer Base;
    ModelRenderer Input5;
    ModelRenderer Input6;
    ModelRenderer Input7;
    ModelRenderer BaseTurn;
    ModelRenderer BaseFrame1;
    ModelRenderer BaseFrame2;
    ModelRenderer BaseFrame3;
    ModelRenderer BaseFrame4;
    ModelRenderer Cannon1;
    ModelRenderer Cannon2;
    ModelRenderer Cannon3;
    ModelRenderer Cannon4;
    ModelRenderer Cannon5;
    ModelRenderer BaseFrame5;
    ModelRenderer BaseFrame6;

    public ModelAirCannon(){
        textureWidth = 64;
        textureHeight = 32;

        Input1 = new ModelRenderer(this, 0, 0);
        Input1.addBox(0F, 0F, 0F, 2, 1, 3);
        Input1.setRotationPoint(-1F, 14F, 5F);
        Input1.setTextureSize(64, 32);
        Input1.mirror = true;
        setRotation(Input1, 0F, 0F, 0F);
        Input2 = new ModelRenderer(this, 0, 0);
        Input2.addBox(0F, 0F, 0F, 2, 7, 1);
        Input2.setRotationPoint(-1F, 17F, 7F);
        Input2.setTextureSize(64, 32);
        Input2.mirror = true;
        setRotation(Input2, 0F, 0F, 0F);
        Input3 = new ModelRenderer(this, 0, 0);
        Input3.addBox(0F, 0F, 0F, 1, 2, 3);
        Input3.setRotationPoint(1F, 15F, 5F);
        Input3.setTextureSize(64, 32);
        Input3.mirror = true;
        setRotation(Input3, 0F, 0F, 0F);
        Input4 = new ModelRenderer(this, 0, 0);
        Input4.addBox(0F, 0F, 0F, 1, 2, 3);
        Input4.setRotationPoint(-2F, 15F, 5F);
        Input4.setTextureSize(64, 32);
        Input4.mirror = true;
        setRotation(Input4, 0F, 0F, 0F);
        Base = new ModelRenderer(this, 8, 15);
        Base.addBox(0F, 0F, 0F, 14, 3, 14);
        Base.setRotationPoint(-7F, 21F, -7F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Input5 = new ModelRenderer(this, 0, 0);
        Input5.addBox(0F, 0F, 0F, 2, 6, 1);
        Input5.setRotationPoint(-1F, 15F, 4F);
        Input5.setTextureSize(64, 32);
        Input5.mirror = true;
        setRotation(Input5, 0F, 0F, 0F);
        Input6 = new ModelRenderer(this, 0, 0);
        Input6.addBox(0F, 0F, 0F, 1, 4, 2);
        Input6.setRotationPoint(1F, 17F, 5F);
        Input6.setTextureSize(64, 32);
        Input6.mirror = true;
        setRotation(Input6, 0F, 0F, 0F);
        Input7 = new ModelRenderer(this, 0, 0);
        Input7.addBox(0F, 0F, 0F, 1, 4, 2);
        Input7.setRotationPoint(-2F, 17F, 5F);
        Input7.setTextureSize(64, 32);
        Input7.mirror = true;
        setRotation(Input7, 0F, 0F, 0F);
        BaseTurn = new ModelRenderer(this, 36, 7);
        BaseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        BaseTurn.setRotationPoint(-3.5F, 20F, -5F);
        BaseTurn.setTextureSize(64, 32);
        BaseTurn.mirror = true;
        setRotation(BaseTurn, 0F, 0F, 0F);
        BaseFrame1 = new ModelRenderer(this, 10, 7);
        BaseFrame1.addBox(0F, 0F, 0F, 1, 5, 3);
        BaseFrame1.setRotationPoint(-3.5F, 15F, -3F);
        BaseFrame1.setTextureSize(64, 32);
        BaseFrame1.mirror = true;
        setRotation(BaseFrame1, 0F, 0F, 0F);
        BaseFrame2 = new ModelRenderer(this, 10, 7);
        BaseFrame2.addBox(0F, 0F, 0F, 1, 5, 3);
        BaseFrame2.setRotationPoint(2.5F, 15F, -3F);
        BaseFrame2.setTextureSize(64, 32);
        BaseFrame2.mirror = true;
        setRotation(BaseFrame2, 0F, 0F, 0F);
        BaseFrame3 = new ModelRenderer(this, 18, 13);
        BaseFrame3.addBox(0F, 0F, 0F, 1, 1, 1);
        BaseFrame3.setRotationPoint(-3.5F, 14F, -2F);
        BaseFrame3.setTextureSize(64, 32);
        BaseFrame3.mirror = true;
        setRotation(BaseFrame3, 0F, 0F, 0F);
        BaseFrame4 = new ModelRenderer(this, 18, 13);
        BaseFrame4.addBox(0F, 0F, 0F, 1, 1, 1);
        BaseFrame4.setRotationPoint(2.5F, 14F, -2F);
        BaseFrame4.setTextureSize(64, 32);
        BaseFrame4.mirror = true;
        setRotation(BaseFrame4, 0F, 0F, 0F);
        Cannon1 = new ModelRenderer(this, 24, 0);
        Cannon1.addBox(0F, 3F, 0F, 2, 1, 2);
        Cannon1.setRotationPoint(-1F, 15F, -2.5F);
        Cannon1.setTextureSize(64, 32);
        Cannon1.mirror = true;
        setRotation(Cannon1, 0F, 0F, 0F);
        Cannon2 = new ModelRenderer(this, 27, 3);
        Cannon2.addBox(0F, 0F, 0F, 2, 8, 1);
        Cannon2.setRotationPoint(-1F, 10F, -0.5F);
        Cannon2.setTextureSize(64, 32);
        Cannon2.mirror = true;
        setRotation(Cannon2, 0F, 0F, 0F);
        Cannon3 = new ModelRenderer(this, 27, 3);
        Cannon3.addBox(0F, 0F, 0F, 2, 8, 1);
        Cannon3.setRotationPoint(-1F, 10F, -3.5F);
        Cannon3.setTextureSize(64, 32);
        Cannon3.mirror = true;
        setRotation(Cannon3, 0F, 0F, 0F);
        Cannon4 = new ModelRenderer(this, 18, 0);
        Cannon4.addBox(0F, 0F, 0F, 1, 8, 2);
        Cannon4.setRotationPoint(-2F, 10F, -2.5F);
        Cannon4.setTextureSize(64, 32);
        Cannon4.mirror = true;
        setRotation(Cannon4, 0F, 0F, 0F);
        Cannon5 = new ModelRenderer(this, 18, 0);
        Cannon5.addBox(0F, 0F, 0F, 1, 8, 2);
        Cannon5.setRotationPoint(1F, 10F, -2.5F);
        Cannon5.setTextureSize(64, 32);
        Cannon5.mirror = true;
        setRotation(Cannon5, 0F, 0F, 0F);
        BaseFrame5 = new ModelRenderer(this, 19, 10);
        BaseFrame5.addBox(0F, 0F, 0F, 1, 1, 1);
        BaseFrame5.setRotationPoint(2F, 15.5F, -2F);
        BaseFrame5.setTextureSize(64, 32);
        BaseFrame5.mirror = true;
        setRotation(BaseFrame5, 0F, 0F, 0F);
        BaseFrame6 = new ModelRenderer(this, 19, 10);
        BaseFrame6.addBox(0F, 0F, 0F, 1, 1, 1);
        BaseFrame6.setRotationPoint(-3F, 15.5F, -2F);
        BaseFrame6.setTextureSize(64, 32);
        BaseFrame6.mirror = true;
        setRotation(BaseFrame6, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Input1.render(f5);
        Input2.render(f5);
        Input3.render(f5);
        Input4.render(f5);
        Base.render(f5);
        Input5.render(f5);
        Input6.render(f5);
        Input7.render(f5);
        BaseTurn.render(f5);
        BaseFrame1.render(f5);
        BaseFrame2.render(f5);
        BaseFrame3.render(f5);
        BaseFrame4.render(f5);
        Cannon1.render(f5);
        Cannon2.render(f5);
        Cannon3.render(f5);
        Cannon4.render(f5);
        Cannon5.render(f5);
        BaseFrame5.render(f5);
        BaseFrame6.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        renderModel(size, 0, 0, false, false);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    public void renderModel(float size, float rotationAngle, float heightAngle, boolean onlyRenderCannon, boolean onlyRenderBase){
        if(!onlyRenderCannon && !onlyRenderBase) {
            Input1.render(size);
            Input2.render(size);
            Input3.render(size);
            Input4.render(size);
            Base.render(size);
            Input5.render(size);
            Input6.render(size);
            Input7.render(size);
        }
        GL11.glPushMatrix();
        if(!onlyRenderCannon) {
            GL11.glTranslated(0.0, 0.0, -0.09375D);
            GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
            GL11.glTranslated(0.0, 0.0, 0.09375D);
            BaseTurn.render(size);
            BaseFrame1.render(size);
            BaseFrame2.render(size);
            BaseFrame3.render(size);
            BaseFrame4.render(size);
            BaseFrame5.render(size);
            BaseFrame6.render(size);
        }
        if(!onlyRenderBase) {
            GL11.glPushMatrix();
            GL11.glTranslated(0.0D, 1.0D, -0.09375D);
            GL11.glRotatef(heightAngle, 1.0F, 0.0F, 0.0F);
            GL11.glTranslated(0.0D, -1.0D, 0.09375D);
            Cannon1.render(size);
            Cannon2.render(size);
            Cannon3.render(size);
            Cannon4.render(size);
            Cannon5.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_AIR_CANNON;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

}
