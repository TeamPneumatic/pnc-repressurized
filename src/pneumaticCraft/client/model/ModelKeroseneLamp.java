package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.client.util.RenderUtils.RenderInfo;
import pneumaticCraft.common.tileentity.TileEntityKeroseneLamp;
import pneumaticCraft.lib.Textures;

public class ModelKeroseneLamp extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Tank;
    ModelRenderer Holder1;
    ModelRenderer Holder2;
    ModelRenderer Base;
    ModelRenderer Top;
    ModelRenderer Support1;
    ModelRenderer SupportSide;
    ModelRenderer SupportSide2;

    public ModelKeroseneLamp(){
        textureWidth = 64;
        textureHeight = 64;

        Tank = new ModelRenderer(this, 0, 43);
        Tank.addBox(0F, 0F, 0F, 6, 8, 6);
        Tank.setRotationPoint(-3F, 15F, -3F);
        Tank.setTextureSize(64, 64);
        Tank.mirror = true;
        setRotation(Tank, 0F, 0F, 0F);
        Holder1 = new ModelRenderer(this, 0, 32);
        Holder1.addBox(0F, 0F, 0F, 1, 10, 1);
        Holder1.setRotationPoint(-5F, 14F, -0.5F);
        Holder1.setTextureSize(64, 32);
        Holder1.mirror = true;
        setRotation(Holder1, 0F, 0F, 0F);
        Holder2 = new ModelRenderer(this, 4, 32);
        Holder2.addBox(0F, 0F, 0F, 1, 10, 1);
        Holder2.setRotationPoint(4F, 14F, -0.5F);
        Holder2.setTextureSize(64, 32);
        Holder2.mirror = true;
        setRotation(Holder2, 0F, 0F, 0F);
        Base = new ModelRenderer(this, 0, 27);
        Base.addBox(0F, 0F, 0F, 8, 1, 4);
        Base.setRotationPoint(-4F, 23F, -2F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 0, 22);
        Top.addBox(0F, 0F, 0F, 8, 1, 4);
        Top.setRotationPoint(-4F, 14F, -2F);
        Top.setTextureSize(64, 32);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        Support1 = new ModelRenderer(this, 0, 15);
        Support1.addBox(0F, 0F, 0F, 1, 6, 1);
        Support1.setRotationPoint(-0.5F, 8F, -0.5F);
        Support1.setTextureSize(64, 32);
        Support1.mirror = true;
        setRotation(Support1, 0F, 0F, 0F);
        SupportSide = new ModelRenderer(this, 0, 0);
        SupportSide.addBox(-0.5F, 0F, -8.5F, 1, 1, 8);
        SupportSide.setRotationPoint(0F, 8F, 0F);
        SupportSide.setTextureSize(64, 32);
        SupportSide.mirror = true;
        setRotation(SupportSide, 0F, 0F, 0F);
        SupportSide2 = new ModelRenderer(this, 4, 16);
        SupportSide2.addBox(-0.5F, -3F, -2.7F, 1, 5, 1);
        SupportSide2.setRotationPoint(0F, 9F, 0F);
        SupportSide2.setTextureSize(64, 32);
        SupportSide2.mirror = true;
        setRotation(SupportSide2, 0.7853982F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Tank.render(f5);
        Holder1.render(f5);
        Holder2.render(f5);
        Base.render(f5);
        Top.render(f5);
        Support1.render(f5);
        SupportSide.render(f5);
        SupportSide2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        ForgeDirection sideConnected = ForgeDirection.DOWN;
        if(te != null) {
            sideConnected = ((TileEntityKeroseneLamp)te).getSideConnected();
        }

        Tank.render(size);
        Holder1.render(size);
        Holder2.render(size);
        Base.render(size);
        Top.render(size);
        if(sideConnected != ForgeDirection.DOWN) {
            Support1.render(size);
            if(sideConnected != ForgeDirection.UP) {
                SupportSide.rotateAngleY = 0;
                ForgeDirection rotation = ((TileEntityKeroseneLamp)te).getRotation();
                if(rotation != ForgeDirection.UP && rotation != ForgeDirection.DOWN) {
                    while(sideConnected != rotation.getOpposite()) {
                        sideConnected = sideConnected.getRotation(ForgeDirection.DOWN);
                        SupportSide.rotateAngleY += Math.toRadians(90);
                    }
                }
                SupportSide2.rotateAngleY = SupportSide.rotateAngleY;
                SupportSide.render(size);
                SupportSide2.render(size);
            }
        }
        if(te != null) {
            FluidTankInfo info = ((TileEntityKeroseneLamp)te).getTankInfo(null)[0];
            if(info.fluid != null && info.fluid.amount > 10) {
                float percentageFull = (float)info.fluid.amount / info.capacity;
                RenderInfo renderInfo = new RenderInfo(-3 / 16F + 0.01F, 23 / 16F - percentageFull * 2.999F / 16F, -3 / 16F + 0.01F, 3 / 16F - 0.01F, 22.99F / 16F, 3 / 16F - 0.01F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, te.getWorldObj());
            }
        }
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_KEROSENE_LAMP;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

}
