package pneumaticCraft.client.model.tubemodules;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.gui.GuiUtils;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.block.tubes.ModulePressureGauge;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class ModelGauge extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModulePressureGauge gaugeModule;

    public ModelGauge(ModulePressureGauge gaugeModule){
        this();
        this.gaugeModule = gaugeModule;
    }

    public ModelGauge(){
        textureWidth = 64;
        textureHeight = 32;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 3, 3, 3);
        Shape1.setRotationPoint(-1.5F, 14.5F, 2F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 6);
        Shape2.addBox(0F, 0F, 0F, 8, 8, 1);
        Shape2.setRotationPoint(-4F, 12F, 5F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
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
        return Textures.MODEL_GAUGE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        Shape1.render(size);
        Shape2.render(size);

        float pressure = 0;
        float dangerPressure = 5;
        float critPressure = 7;
        if(gaugeModule != null && gaugeModule.getTube() != null) {
            TileEntityPneumaticBase base = (TileEntityPneumaticBase)((IPneumaticMachine)gaugeModule.getTube()).getAirHandler();
            pressure = base.getPressure(ForgeDirection.UNKNOWN);
            dangerPressure = base.DANGER_PRESSURE;
            critPressure = base.CRITICAL_PRESSURE;
        }
        GL11.glTranslated(0, 1, 0.378);
        double scale = 0.008D;
        GL11.glScaled(scale, scale, scale);
        GL11.glRotated(180, 0, 1, 0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiUtils.drawPressureGauge(FMLClientHandler.instance().getClient().fontRenderer, -1, critPressure, dangerPressure, -1, pressure, 0, 0, 0);
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
