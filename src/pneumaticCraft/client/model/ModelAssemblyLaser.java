package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.lib.Textures;

public class ModelAssemblyLaser extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer BaseTurn;
    ModelRenderer BaseTurn2;
    ModelRenderer ArmBase1;
    ModelRenderer ArmBase2;
    ModelRenderer SupportMiddle;
    ModelRenderer ArmMiddle1;
    ModelRenderer ArmMiddle2;
    ModelRenderer LaserBase;
    ModelRenderer Laser;

    public ModelAssemblyLaser(){
        textureWidth = 64;
        textureHeight = 64;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 1F, 16, 1, 16);
        Base.setRotationPoint(-8F, 23F, -9F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        BaseTurn = new ModelRenderer(this, 0, 17);
        BaseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        BaseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        BaseTurn.setTextureSize(64, 32);
        BaseTurn.mirror = true;
        setRotation(BaseTurn, 0F, 0F, 0F);
        BaseTurn2 = new ModelRenderer(this, 28, 17);
        BaseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        BaseTurn2.setRotationPoint(-2F, 17F, -2F);
        BaseTurn2.setTextureSize(64, 32);
        BaseTurn2.mirror = true;
        setRotation(BaseTurn2, 0F, 0F, 0F);
        ArmBase1 = new ModelRenderer(this, 0, 25);
        ArmBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        ArmBase1.setRotationPoint(2F, 17F, -1F);
        ArmBase1.setTextureSize(64, 32);
        ArmBase1.mirror = true;
        setRotation(ArmBase1, 0F, 0F, 0F);
        ArmBase2 = new ModelRenderer(this, 0, 25);
        ArmBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        ArmBase2.setRotationPoint(-3F, 17F, -1F);
        ArmBase2.setTextureSize(64, 32);
        ArmBase2.mirror = true;
        setRotation(ArmBase2, 0F, 0F, 0F);
        SupportMiddle = new ModelRenderer(this, 0, 57);
        SupportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        SupportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        SupportMiddle.setTextureSize(64, 32);
        SupportMiddle.mirror = true;
        setRotation(SupportMiddle, 0F, 0F, 0F);
        ArmMiddle1 = new ModelRenderer(this, 0, 35);
        ArmMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        ArmMiddle1.setRotationPoint(-2F, 2F, 5F);
        ArmMiddle1.setTextureSize(64, 32);
        ArmMiddle1.mirror = true;
        setRotation(ArmMiddle1, 0F, 0F, 0F);
        ArmMiddle2 = new ModelRenderer(this, 0, 35);
        ArmMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        ArmMiddle2.setRotationPoint(1F, 2F, 5F);
        ArmMiddle2.setTextureSize(64, 32);
        ArmMiddle2.mirror = true;
        setRotation(ArmMiddle2, 0F, 0F, 0F);
        LaserBase = new ModelRenderer(this, 8, 38);
        LaserBase.addBox(0F, 0F, 0F, 2, 2, 3);
        LaserBase.setRotationPoint(-1F, 2F, 4.5F);
        LaserBase.setTextureSize(64, 32);
        LaserBase.mirror = true;
        setRotation(LaserBase, 0F, 0F, 0F);
        Laser = new ModelRenderer(this, 54, 59);
        Laser.addBox(0F, 0F, 0F, 1, 1, 32);
        Laser.setRotationPoint(-0.5F, 2.5F, 1F);
        Laser.setTextureSize(64, 32);
        Laser.mirror = true;
        setRotation(Laser, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        BaseTurn.render(f5);
        BaseTurn2.render(f5);
        ArmBase1.render(f5);
        ArmBase2.render(f5);
        SupportMiddle.render(f5);
        ArmMiddle1.render(f5);
        ArmMiddle2.render(f5);
        LaserBase.render(f5);
        Laser.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityAssemblyLaser) {
            TileEntityAssemblyLaser tile = (TileEntityAssemblyLaser)te;
            float[] renderAngles = new float[5];
            for(int i = 0; i < 5; i++) {
                renderAngles[i] = tile.oldAngles[i] + (tile.angles[i] - tile.oldAngles[i]) * partialTicks;
            }
            renderModel(0.0625F, renderAngles, tile.isLaserOn);
        } else {
            renderModel(size, new float[]{0, 0, 35, 55, 0}, false);
        }
    }

    public void renderModel(float size, float[] angles, boolean laserOn){
        Base.render(size);
        GL11.glPushMatrix();
        GL11.glRotatef(angles[0], 0, 1, 0);
        BaseTurn.render(size);
        BaseTurn2.render(size);
        GL11.glTranslated(0, 18 / 16F, 0);
        GL11.glRotatef(angles[1], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, 0);
        ArmBase1.render(size);
        ArmBase2.render(size);
        SupportMiddle.render(size);
        GL11.glTranslated(0, 18 / 16F, 6 / 16F);
        GL11.glRotatef(angles[2], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, -6 / 16F);
        ArmMiddle1.render(size);
        ArmMiddle2.render(size);
        GL11.glTranslated(0, 3 / 16F, 6 / 16F);
        GL11.glRotatef(angles[3], 1, 0, 0);
        GL11.glTranslated(0, -3 / 16F, -6 / 16F);
        LaserBase.render(size);
        if(laserOn) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 2.75 / 16D, 1 / 16D);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4d(1.0D, 0.1D, 0, 1);
            // GL11.glTranslated(0, 3 / 16F, 0);
            //  GL11.glRotatef(angles[4], 0, 0, 1);
            //  GL11.glTranslated(0, -3 / 16F, 0);

            Laser.render(size / 8);
            GL11.glPopMatrix();
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        double textSize = 1 / 150D;
        GL11.glScaled(textSize, textSize, textSize);
        GL11.glRotated(-90, 1, 0, 0);
        GL11.glTranslated(0, 0, 18);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiPneumaticContainerBase.drawTexture(Textures.GUI_LASER_DANGER, -8, -65);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_ASSEMBLY_LASER_AND_DRILL;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
