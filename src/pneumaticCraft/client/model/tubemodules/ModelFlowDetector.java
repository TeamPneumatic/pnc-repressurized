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
    ModelRenderer Shape1;
    private ModuleFlowDetector flowDetector;

    public ModelFlowDetector(ModuleFlowDetector flowDetector){
        this();
        this.flowDetector = flowDetector;
    }

    public ModelFlowDetector(){
        textureWidth = 64;
        textureHeight = 32;

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
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_FLOW_DETECTOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        int parts = 9;
        for(int i = 0; i < parts; i++) {
            Shape1.rotateAngleZ = (float)i / parts * 2 * (float)Math.PI + (flowDetector != null ? flowDetector.oldRotation + (flowDetector.rotation - flowDetector.oldRotation) * partialTicks : 0);
            Shape1.render(size);
        }
    }

}
