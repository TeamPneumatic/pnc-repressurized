package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.Textures;

public class ModelChargingStation extends ModelBase implements IBaseModel{

    private final RenderItem customRenderItem;
    private final ModelChargingStationPad chargePad = new ModelChargingStationPad();

    ModelRenderer Back1;
    ModelRenderer Back2;
    ModelRenderer Back3;
    ModelRenderer Back4;
    ModelRenderer Base;
    ModelRenderer Platform;
    ModelRenderer InputSmall1;
    ModelRenderer InputSmall2;
    ModelRenderer InputSmall3;
    ModelRenderer InputSmall4;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;

    public ModelChargingStation(){
        textureWidth = 64;
        textureHeight = 32;

        Back1 = new ModelRenderer(this, 0, 0);
        Back1.addBox(0F, 0F, 0F, 2, 1, 4);
        Back1.setRotationPoint(-1F, 14F, 4F);
        Back1.setTextureSize(64, 32);
        Back1.mirror = true;
        setRotation(Back1, 0F, 0F, 0F);
        Back2 = new ModelRenderer(this, 0, 0);
        Back2.addBox(0F, 0F, 0F, 2, 1, 4);
        Back2.setRotationPoint(-1F, 17F, 4F);
        Back2.setTextureSize(64, 32);
        Back2.mirror = true;
        setRotation(Back2, 0F, 0F, 0F);
        Back3 = new ModelRenderer(this, 0, 0);
        Back3.addBox(0F, 0F, 0F, 1, 2, 4);
        Back3.setRotationPoint(1F, 15F, 4F);
        Back3.setTextureSize(64, 32);
        Back3.mirror = true;
        setRotation(Back3, 0F, 0F, 0F);
        Back4 = new ModelRenderer(this, 0, 0);
        Back4.addBox(0F, 0F, 0F, 1, 2, 4);
        Back4.setRotationPoint(-2F, 15F, 4F);
        Back4.setTextureSize(64, 32);
        Back4.mirror = true;
        setRotation(Back4, 0F, 0F, 0F);
        Base = new ModelRenderer(this, 0, 17);
        Base.addBox(0F, 0F, 0F, 14, 1, 14);
        Base.setRotationPoint(-7F, 23F, -7F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Platform = new ModelRenderer(this, 0, 8);
        Platform.addBox(0F, 0F, 0F, 8, 1, 8);
        Platform.setRotationPoint(-4F, 18F, -4F);
        Platform.setTextureSize(64, 32);
        Platform.mirror = true;
        setRotation(Platform, 0F, 0F, 0F);
        InputSmall1 = new ModelRenderer(this, 0, 0);
        InputSmall1.addBox(0F, 0F, 0F, 1, 1, 3);
        InputSmall1.setRotationPoint(-0.5F, 14.5F, 1F);
        InputSmall1.setTextureSize(64, 32);
        InputSmall1.mirror = true;
        setRotation(InputSmall1, 0F, 0F, 0F);
        InputSmall2 = new ModelRenderer(this, 0, 0);
        InputSmall2.addBox(0F, 0F, 0F, 1, 1, 3);
        InputSmall2.setRotationPoint(0.5F, 15.5F, 1F);
        InputSmall2.setTextureSize(64, 32);
        InputSmall2.mirror = true;
        setRotation(InputSmall2, 0F, 0F, 0F);
        InputSmall3 = new ModelRenderer(this, 0, 0);
        InputSmall3.addBox(0F, 0F, 0F, 1, 1, 3);
        InputSmall3.setRotationPoint(-0.5F, 16.5F, 1F);
        InputSmall3.setTextureSize(64, 32);
        InputSmall3.mirror = true;
        setRotation(InputSmall3, 0F, 0F, 0F);
        InputSmall4 = new ModelRenderer(this, 0, 0);
        InputSmall4.addBox(0F, 0F, 0F, 1, 1, 3);
        InputSmall4.setRotationPoint(-1.5F, 15.5F, 1F);
        InputSmall4.setTextureSize(64, 32);
        InputSmall4.mirror = true;
        setRotation(InputSmall4, 0F, 0F, 0F);
        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape1.setRotationPoint(-1.5F, 14.5F, 3F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape2.setRotationPoint(0.5F, 14.5F, 3F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape3.setRotationPoint(0.5F, 16.5F, 3F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 0);
        Shape4.addBox(0F, 0F, 0F, 1, 1, 1);
        Shape4.setRotationPoint(-1.5F, 16.5F, 3F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Leg1 = new ModelRenderer(this, 15, 0);
        Leg1.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg1.setRotationPoint(2.3F, 18F, -3F);
        Leg1.setTextureSize(64, 32);
        Leg1.mirror = true;
        setRotation(Leg1, -0.5585054F, -0.7853982F, 0F);
        Leg2 = new ModelRenderer(this, 15, 0);
        Leg2.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg2.setRotationPoint(-3F, 18F, -2.3F);
        Leg2.setTextureSize(64, 32);
        Leg2.mirror = true;
        setRotation(Leg2, -0.5585054F, 0.7853982F, 0F);
        Leg3 = new ModelRenderer(this, 15, 0);
        Leg3.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg3.setRotationPoint(3F, 18F, 2.3F);
        Leg3.setTextureSize(64, 32);
        Leg3.mirror = true;
        setRotation(Leg3, -0.5585054F, -2.356194F, 0F);
        Leg4 = new ModelRenderer(this, 15, 0);
        Leg4.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg4.setRotationPoint(-2.3F, 18F, 3F);
        Leg4.setTextureSize(64, 32);
        Leg4.mirror = true;
        setRotation(Leg4, -0.5585054F, 2.356194F, 0F);

        customRenderItem = new RenderItem(){
            @Override
            public boolean shouldBob(){

                return false;
            };
        };
        customRenderItem.setRenderManager(RenderManager.instance);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Back1.render(f5);
        Back2.render(f5);
        Back3.render(f5);
        Back4.render(f5);
        Base.render(f5);
        Platform.render(f5);
        InputSmall1.render(f5);
        InputSmall2.render(f5);
        InputSmall3.render(f5);
        InputSmall4.render(f5);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
        Leg1.render(f5);
        Leg2.render(f5);
        Leg3.render(f5);
        Leg4.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        renderModel(size);
        if(te instanceof TileEntityChargingStation) {
            TileEntityChargingStation tile = (TileEntityChargingStation)te;
            if(tile.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
                RenderManager.instance.renderEngine.bindTexture(Textures.MODEL_CHARGING_STATION_PAD);
                chargePad.renderModel(size);
            }
            if(tile.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) != null) {
                float scaleFactor = 0.7F;

                EntityItem ghostEntityItem = new EntityItem(tile.getWorldObj());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setEntityItemStack(tile.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX));

                GL11.glTranslated(0, 1, 0);
                GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
                GL11.glScalef(1.0F, -1F, -1F);

                boolean fancySetting = RenderManager.instance.options.fancyGraphics;
                RenderManager.instance.options.fancyGraphics = true;
                customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
                RenderManager.instance.options.fancyGraphics = fancySetting;
            }
        }
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    public void renderModel(float size){
        Back1.render(size);
        Back2.render(size);
        Back3.render(size);
        Back4.render(size);
        Base.render(size);
        Platform.render(size);
        InputSmall1.render(size);
        InputSmall2.render(size);
        InputSmall3.render(size);
        InputSmall4.render(size);
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Leg1.render(size);
        Leg2.render(size);
        Leg3.render(size);
        Leg4.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_CHARGING_STATION;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

}
