// Date: 1-10-2014 18:10:04
package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTankInfo;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.client.util.RenderUtils.RenderInfo;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.lib.Textures;

public class ModelPlasticMixer extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape2;
    ModelRenderer Shape3b;
    ModelRenderer Shape1;
    ModelRenderer Shape3;
    ModelRenderer Shape4b;
    ModelRenderer Shape4;

    public ModelPlasticMixer(){
        textureWidth = 128;
        textureHeight = 128;

        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 12, 14, 12);
        Shape2.setRotationPoint(-6F, 8F, -6F);
        Shape2.setTextureSize(128, 128);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3b = new ModelRenderer(this, 0, 38);
        Shape3b.addBox(0F, 0F, 0F, 14, 8, 1);
        Shape3b.setRotationPoint(-7F, 14F, 6F);
        Shape3b.setTextureSize(128, 128);
        Shape3b.mirror = true;
        setRotation(Shape3b, 0F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 50, 0);
        Shape1.addBox(0F, 0F, 0F, 14, 2, 14);
        Shape1.setRotationPoint(-7F, 22F, -7F);
        Shape1.setTextureSize(128, 128);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 27);
        Shape3.addBox(0F, 0F, 0F, 14, 8, 1);
        Shape3.setRotationPoint(-7F, 14F, -7F);
        Shape3.setTextureSize(128, 128);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4b = new ModelRenderer(this, 0, 70);
        Shape4b.addBox(0F, 0F, 0F, 1, 8, 12);
        Shape4b.setRotationPoint(-7F, 14F, -6F);
        Shape4b.setTextureSize(128, 128);
        Shape4b.mirror = true;
        setRotation(Shape4b, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 48);
        Shape4.addBox(0F, 0F, 0F, 1, 8, 12);
        Shape4.setRotationPoint(6F, 14F, -6F);
        Shape4.setTextureSize(128, 128);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape2.render(f5);
        Shape3b.render(f5);
        Shape1.render(f5);
        Shape3.render(f5);
        Shape4b.render(f5);
        Shape4.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        Shape2.render(size);
        Shape3b.render(size);
        Shape1.render(size);
        Shape3.render(size);
        Shape4b.render(size);
        Shape4.render(size);

        if(te != null) {
            TileEntityPlasticMixer mixer = (TileEntityPlasticMixer)te;
            FluidTankInfo info = mixer.getTankInfo(null)[0];
            if(info.fluid != null && info.fluid.amount > 10) {
                float percentageFull = (float)info.fluid.amount / info.capacity;
                RenderInfo renderInfo = new RenderInfo(-6 / 16F + 0.01F, 22 / 16F - percentageFull * 13.999F / 16F, -6 / 16F + 0.01F, 6 / 16F - 0.01F, 22 / 16F, 6 / 16F - 0.01F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, mixer.getWorldObj());
            }
        }
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PLASTIC_MIXER;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
