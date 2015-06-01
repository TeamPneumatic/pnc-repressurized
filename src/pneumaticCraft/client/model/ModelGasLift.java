package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import pneumaticCraft.common.tileentity.TileEntityGasLift;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class ModelGasLift extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    private final ModelPressureTube tubeRenderer = new ModelPressureTube();
    private final ModuleRegulatorTube module = new ModuleRegulatorTube();

    public ModelGasLift(){
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 24);
        Shape1.addBox(0F, 0F, 0F, 16, 2, 16);
        Shape1.setRotationPoint(-8F, 22F, -8F);
        Shape1.setTextureSize(64, 64);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 8, 2, 8);
        Shape2.setRotationPoint(-4F, 18F, -4F);
        Shape2.setTextureSize(64, 64);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 10);
        Shape3.addBox(0F, 0F, 0F, 12, 2, 12);
        Shape3.setRotationPoint(-6F, 20F, -6F);
        Shape3.setTextureSize(64, 64);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_PRESSURE_TUBE);
        if(te != null) {
            boolean[] sidesConnected = ((TileEntityGasLift)te).sidesConnected;
            tubeRenderer.renderModel(size, sidesConnected);
            GL11.glScalef(1.0F, -1F, -1F);
            for(int i = 0; i < 6; i++) {
                if(sidesConnected[i]) {
                    module.setDirection(ForgeDirection.getOrientation(i));
                    module.renderDynamic(-0.5, -1.5, -0.5, 0, 0, false);
                }
            }
        } else {
            tubeRenderer.renderModel(size, new boolean[6]);
        }
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){}

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_GAS_LIFT;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
