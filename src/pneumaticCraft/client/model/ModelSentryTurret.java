package pneumaticCraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.entity.ModelDroneMinigun;
import pneumaticCraft.common.tileentity.TileEntitySentryTurret;
import pneumaticCraft.lib.Textures;

public class ModelSentryTurret extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer tripot1;
    ModelRenderer tripot2;
    ModelRenderer tripot3;
    ModelRenderer main;
    ModelRenderer main2;
    private final ModelDroneMinigun model = new ModelDroneMinigun();
    private final TileEntitySentryTurret fakeTurret = new TileEntitySentryTurret();

    public ModelSentryTurret(){
        textureWidth = 64;
        textureHeight = 32;

        tripot1 = new ModelRenderer(this, 0, 0);
        tripot1.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot1.setRotationPoint(-0.5F, 18F, -1F);
        tripot1.setTextureSize(64, 32);
        tripot1.mirror = true;
        setRotation(tripot1, -0.6981317F, 0F, 0F);
        tripot2 = new ModelRenderer(this, 4, 0);
        tripot2.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot2.setRotationPoint(1F, 18F, 0F);
        tripot2.setTextureSize(64, 32);
        tripot2.mirror = true;
        setRotation(tripot2, -0.6981317F, -2.094395F, 0F);
        tripot3 = new ModelRenderer(this, 8, 0);
        tripot3.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot3.setRotationPoint(-0.5F, 18F, 1F);
        tripot3.setTextureSize(64, 32);
        tripot3.mirror = true;
        setRotation(tripot3, -0.6981317F, 2.094395F, 0F);
        main = new ModelRenderer(this, 12, 0);
        main.addBox(0F, 0F, 0F, 2, 1, 2);
        main.setRotationPoint(-1F, 17.5F, -1F);
        main.setTextureSize(64, 32);
        main.mirror = true;
        setRotation(main, 0F, 0F, 0F);
        main2 = new ModelRenderer(this, 12, 3);
        main2.addBox(0F, 0F, 0F, 1, 2, 1);
        main2.setRotationPoint(-0.5F, 16F, -0.5F);
        main2.setTextureSize(64, 32);
        main2.mirror = true;
        setRotation(main2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        tripot1.render(f5);
        tripot2.render(f5);
        tripot3.render(f5);
        main.render(f5);
        main2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        tripot1.render(size);
        tripot2.render(size);
        tripot3.render(size);
        main.render(size);
        main2.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

        GL11.glPushMatrix();
        GL11.glTranslated(0, -13 / 16D, 0);
        if(te == null) {
            model.renderMinigun(fakeTurret.getMinigun(), 1 / 16F, partialTicks, false);
        } else {
            TileEntitySentryTurret tile = (TileEntitySentryTurret)te;
            model.renderMinigun(tile.getMinigun(), 1 / 16F, partialTicks, false);

            GL11.glPushMatrix();
            GL11.glScalef(1.0F, -1, -1F);
            GL11.glTranslated(0, -1.45F, 0);
            tile.getMinigun().render(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, 1.2);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(getModelTexture(te));
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_SENTRY_TURRET;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
