package pneumaticCraft.client.model.tubemodules;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.block.tubes.ModuleFlowDetector;
import pneumaticCraft.lib.Textures;

public class ModelFlowDetector extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Back1;
    ModelRenderer Back2;
    ModelRenderer Back3;
    ModelRenderer Back4;
    ModelRenderer Shape1;
    private ModuleFlowDetector flowDetector;

    public ModelFlowDetector(ModuleFlowDetector flowDetector){
        this();
        this.flowDetector = flowDetector;
    }

    public ModelFlowDetector(){
        textureWidth = 64;
        textureHeight = 32;

        Back1 = new ModelRenderer(this, 0, 0);
        Back1.addBox(0F, 0F, 0F, 2, 1, 6);
        Back1.setRotationPoint(-1F, 14F, 2F);
        Back1.setTextureSize(64, 32);
        Back1.mirror = true;
        setRotation(Back1, 0F, 0F, 0F);
        Back2 = new ModelRenderer(this, 0, 0);
        Back2.addBox(0F, 0F, 0F, 2, 1, 6);
        Back2.setRotationPoint(-1F, 17F, 2F);
        Back2.setTextureSize(64, 32);
        Back2.mirror = true;
        setRotation(Back2, 0F, 0F, 0F);
        Back3 = new ModelRenderer(this, 0, 0);
        Back3.addBox(0F, 0F, 0F, 1, 2, 6);
        Back3.setRotationPoint(1F, 15F, 2F);
        Back3.setTextureSize(64, 32);
        Back3.mirror = true;
        setRotation(Back3, 0F, 0F, 0F);
        Back4 = new ModelRenderer(this, 0, 0);
        Back4.addBox(0F, 0F, 0F, 1, 2, 6);
        Back4.setRotationPoint(-2F, 15F, 2F);
        Back4.setTextureSize(64, 32);
        Back4.mirror = true;
        setRotation(Back4, 0F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 0, 8);
        Shape1.addBox(-1F, -3F, -2F, 2, 1, 5);
        Shape1.setRotationPoint(0F, 16F, 4.5F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Back1.render(f5);
        Back2.render(f5);
        Back3.render(f5);
        Back4.render(f5);
        Shape1.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){

    }

    @Override
    public ResourceLocation getModelTexture(){
        return Textures.MODEL_FLOW_DETECTOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        Back1.render(size);
        Back2.render(size);
        Back3.render(size);
        Back4.render(size);
        int parts = 9;
        for(int i = 0; i < parts; i++) {
            Shape1.rotateAngleZ = (float)i / parts * 2 * (float)Math.PI + (flowDetector != null ? flowDetector.oldRotation + (flowDetector.rotation - flowDetector.oldRotation) * partialTicks : 0);
            Shape1.render(size);
        }
    }

}
