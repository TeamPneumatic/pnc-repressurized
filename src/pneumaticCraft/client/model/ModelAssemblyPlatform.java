package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.client.assemblymachine.AssemblyRenderOverriding;
import pneumaticCraft.api.client.assemblymachine.AssemblyRenderOverriding.IAssemblyRenderOverriding;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;
import pneumaticCraft.lib.Textures;

public class ModelAssemblyPlatform extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer Platform;
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Claw1;
    ModelRenderer Claw2;
    private final RenderItem customRenderItem;

    public ModelAssemblyPlatform(){
        // EE3 snippet, to initialize an EntityItem which doesn't bob.
        customRenderItem = new RenderItem(){
            @Override
            public boolean shouldBob(){

                return false;
            };
        };
        customRenderItem.setRenderManager(RenderManager.instance);

        textureWidth = 64;
        textureHeight = 32;

        Base = new ModelRenderer(this, 0, 15);
        Base.addBox(0F, 0F, 0F, 16, 1, 16);
        Base.setRotationPoint(-8F, 23F, -8F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Platform = new ModelRenderer(this, 0, 6);
        Platform.addBox(0F, 0F, 0F, 8, 1, 8);
        Platform.setRotationPoint(-4F, 18F, -4F);
        Platform.setTextureSize(64, 32);
        Platform.mirror = true;
        setRotation(Platform, 0F, 0F, 0F);
        Leg1 = new ModelRenderer(this, 34, 0);
        Leg1.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg1.setRotationPoint(2.3F, 18F, -3F);
        Leg1.setTextureSize(64, 32);
        Leg1.mirror = true;
        setRotation(Leg1, -0.5585054F, -0.7853982F, 0F);
        Leg2 = new ModelRenderer(this, 34, 0);
        Leg2.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg2.setRotationPoint(-3F, 18F, -2.3F);
        Leg2.setTextureSize(64, 32);
        Leg2.mirror = true;
        setRotation(Leg2, -0.5585054F, 0.7853982F, 0F);
        Leg3 = new ModelRenderer(this, 34, 0);
        Leg3.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg3.setRotationPoint(3F, 18F, 2.3F);
        Leg3.setTextureSize(64, 32);
        Leg3.mirror = true;
        setRotation(Leg3, -0.5585054F, -2.356194F, 0F);
        Leg4 = new ModelRenderer(this, 34, 0);
        Leg4.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg4.setRotationPoint(-2.3F, 18F, 3F);
        Leg4.setTextureSize(64, 32);
        Leg4.mirror = true;
        setRotation(Leg4, -0.5585054F, 2.356194F, 0F);
        Claw1 = new ModelRenderer(this, 0, 0);
        Claw1.addBox(0F, 0F, 0F, 2, 1, 1);
        Claw1.setRotationPoint(-1F, 17F, 0F);
        Claw1.setTextureSize(64, 32);
        Claw1.mirror = true;
        setRotation(Claw1, 0F, 0F, 0F);
        Claw2 = new ModelRenderer(this, 0, 0);
        Claw2.addBox(0F, 0F, 0F, 2, 1, 1);
        Claw2.setRotationPoint(-1F, 17F, -1F);
        Claw2.setTextureSize(64, 32);
        Claw2.mirror = true;
        setRotation(Claw2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        Platform.render(f5);
        Leg1.render(f5);
        Leg2.render(f5);
        Leg3.render(f5);
        Leg4.render(f5);
        Claw1.render(f5);
        Claw2.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    public void renderModel(float size, float progress, EntityItem carriedItem){
        float clawTrans;
        float scaleFactor = 0.7F;

        IAssemblyRenderOverriding renderOverride = null;
        if(carriedItem != null) {
            renderOverride = AssemblyRenderOverriding.renderOverrides.get(carriedItem.getEntityItem());
            if(renderOverride != null) {
                clawTrans = renderOverride.getPlatformClawShift(carriedItem.getEntityItem());
            } else {
                if(carriedItem.getEntityItem().getItem() instanceof ItemBlock) {
                    clawTrans = 1.5F / 16F - progress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - progress * 1.4F / 16F;
                    scaleFactor = 0.4F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - progress * 1.5F / 16F;
        }
        Base.render(size);
        Platform.render(size);
        Leg1.render(size);
        Leg2.render(size);
        Leg3.render(size);
        Leg4.render(size);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, clawTrans);
        Claw1.render(size);
        GL11.glTranslated(0, 0, -2 * clawTrans);
        Claw2.render(size);
        GL11.glPopMatrix();
        if(carriedItem != null) {
            if(renderOverride == null || renderOverride.applyRenderChangePlatform(carriedItem.getEntityItem())) {
                GL11.glRotated(180, 1, 0, 0);
                GL11.glTranslated(0, carriedItem.getEntityItem().getItem() instanceof ItemBlock ? -16.5 / 16F : -17.5 / 16F, 0);
                // GL11.glRotated(-90, 0, 1, 0);

                GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
                customRenderItem.doRender(carriedItem, 0, 0, 0, 0, 0);
            }
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_ASSEMBLY_PLATFORM;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityAssemblyPlatform) {
            TileEntityAssemblyPlatform tile = (TileEntityAssemblyPlatform)te;
            EntityItem ghostEntityItem = null;
            if(tile.getHeldStack() != null) {
                ghostEntityItem = new EntityItem(tile.getWorldObj());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setEntityItemStack(tile.getHeldStack());
            }
            boolean fancySetting = RenderManager.instance.options.fancyGraphics;
            RenderManager.instance.options.fancyGraphics = true;
            renderModel(size, tile.oldClawProgress + (tile.clawProgress - tile.oldClawProgress) * partialTicks, ghostEntityItem);
            RenderManager.instance.options.fancyGraphics = fancySetting;
        } else {
            renderModel(size, 0, null);
        }
    }

}
