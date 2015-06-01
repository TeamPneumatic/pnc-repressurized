package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTankInfo;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.client.util.RenderUtils.RenderInfo;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.lib.Textures;

public class ModelRefinery extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer outputTank;
    ModelRenderer inputTank;
    ModelRenderer machineLeft;
    ModelRenderer machineRight;
    ModelRenderer machineMiddle;

    public ModelRefinery(){
        textureWidth = 64;
        textureHeight = 128;

        outputTank = new ModelRenderer(this, 26, 0);
        outputTank.addBox(0F, 0F, 0F, 8, 16, 5);
        outputTank.setRotationPoint(-4F, 8F, -8F);
        outputTank.setTextureSize(64, 128);
        outputTank.mirror = true;
        setRotation(outputTank, 0F, 0F, 0F);
        inputTank = new ModelRenderer(this, 0, 0);
        inputTank.addBox(0F, 0F, 0F, 8, 16, 5);
        inputTank.setRotationPoint(-4F, 8F, 3F);
        inputTank.setTextureSize(64, 128);
        inputTank.mirror = true;
        setRotation(inputTank, 0F, 0F, 0F);
        machineLeft = new ModelRenderer(this, 0, 26);
        machineLeft.addBox(0F, 0F, 0F, 4, 16, 10);
        machineLeft.setRotationPoint(-8F, 8F, -5F);
        machineLeft.setTextureSize(64, 128);
        machineLeft.mirror = true;
        setRotation(machineLeft, 0F, 0F, 0F);
        machineRight = new ModelRenderer(this, 0, 52);
        machineRight.addBox(0F, 0F, 0F, 4, 16, 10);
        machineRight.setRotationPoint(4F, 8F, -5F);
        machineRight.setTextureSize(64, 128);
        machineRight.mirror = true;
        setRotation(machineRight, 0F, 0F, 0F);
        machineMiddle = new ModelRenderer(this, 28, 26);
        machineMiddle.addBox(0F, 0F, 0F, 8, 16, 6);
        machineMiddle.setRotationPoint(-4F, 8F, -3F);
        machineMiddle.setTextureSize(64, 128);
        machineMiddle.mirror = true;
        setRotation(machineMiddle, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        outputTank.render(f5);
        inputTank.render(f5);
        machineLeft.render(f5);
        machineRight.render(f5);
        machineMiddle.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        outputTank.render(size);
        inputTank.render(size);
        machineLeft.render(size);
        machineRight.render(size);
        machineMiddle.render(size);

        if(te != null) {
            TileEntityRefinery refinery = (TileEntityRefinery)te;
            FluidTankInfo info = new FluidTankInfo(refinery.getOilTank());
            if(info.fluid != null && info.fluid.amount > 10) {
                float percentageFull = (float)info.fluid.amount / info.capacity;
                RenderInfo renderInfo = new RenderInfo(-4 / 16F + 0.01F, 22 / 16F - percentageFull * 13.999F / 16F, 3 / 16F + 0.01F, 4 / 16F - 0.01F, 22 / 16F, 8 / 16F - 0.01F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, refinery.getWorldObj());
            }

            info = refinery.getTankInfo(null)[1];
            if(info.fluid != null && info.fluid.amount > 10) {
                float percentageFull = (float)info.fluid.amount / info.capacity;
                RenderInfo renderInfo = new RenderInfo(-4 / 16F + 0.01F, 24 / 16F - percentageFull * 15.999F / 16F, -8 / 16F + 0.01F, 4 / 16F - 0.01F, 24 / 16F, -3 / 16F - 0.01F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, refinery.getWorldObj());
            }
        }
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_REFINERY;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

}
