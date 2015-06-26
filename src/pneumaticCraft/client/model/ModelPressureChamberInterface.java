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

import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;
import pneumaticCraft.lib.Textures;

public class ModelPressureChamberInterface extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Input;
    ModelRenderer Output;

    private final RenderItem customRenderItem;

    public ModelPressureChamberInterface(){
        textureWidth = 128;
        textureHeight = 128;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 13, 3, 16);
        Shape1.setRotationPoint(-5F, 21F, -8F);
        Shape1.setTextureSize(128, 128);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 19);
        Shape2.addBox(0F, 0F, 0F, 3, 13, 16);
        Shape2.setRotationPoint(5F, 8F, -8F);
        Shape2.setTextureSize(128, 128);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new ModelRenderer(this, 58, 0);
        Shape3.addBox(0F, 0F, 0F, 13, 3, 16);
        Shape3.setRotationPoint(-8F, 8F, -8F);
        Shape3.setTextureSize(128, 128);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 38, 19);
        Shape4.addBox(0F, 0F, 0F, 3, 13, 16);
        Shape4.setRotationPoint(-8F, 11F, -8F);
        Shape4.setTextureSize(128, 128);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 48);
        Shape5.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape5.setRotationPoint(-5F, 11F, -8F);
        Shape5.setTextureSize(128, 128);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 36, 48);
        Shape6.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape6.setRotationPoint(3F, 11F, -8F);
        Shape6.setTextureSize(128, 128);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 66);
        Shape7.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape7.setRotationPoint(-5F, 19F, -8F);
        Shape7.setTextureSize(128, 128);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 36, 66);
        Shape8.addBox(0F, 0F, 0F, 2, 2, 16);
        Shape8.setRotationPoint(3F, 19F, -8F);
        Shape8.setTextureSize(128, 128);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Input = new ModelRenderer(this, 0, 84);
        Input.addBox(0F, 0F, 0F, 10, 10, 2);
        Input.setRotationPoint(-5F, 11F, -7.2F);
        Input.setTextureSize(128, 128);
        Input.mirror = true;
        setRotation(Input, 0F, 0F, 0F);
        Output = new ModelRenderer(this, 24, 84);
        Output.addBox(0F, 0F, 0F, 10, 10, 2);
        Output.setRotationPoint(-5F, 11F, 5.2F);
        Output.setTextureSize(128, 128);
        Output.mirror = true;
        setRotation(Output, 0F, 0F, 0F);
        setRotation(Output, 0F, 0F, 0F);

        // EE3 snippet, to initialize an EntityItem which doesn't bob.
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
        Shape1.render(f5);
        Shape2.render(f5);
        Shape3.render(f5);
        Shape4.render(f5);
        Shape5.render(f5);
        Shape6.render(f5);
        Shape7.render(f5);
        Shape8.render(f5);
        Input.render(f5);
        Output.render(f5);
    }

    public void renderModel(float size, float inputDoor, float outputDoor){
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        GL11.glPushMatrix();
        GL11.glTranslatef((1F - (float)Math.cos(inputDoor * Math.PI)) * 0.37F, 0, 0);
        Input.render(size);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((1F - (float)Math.cos(outputDoor * Math.PI)) * 0.37F, 0, 0);
        Output.render(size);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PRESSURE_CHAMBER_INTERFACE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityPressureChamberInterface) {
            TileEntityPressureChamberInterface tile = (TileEntityPressureChamberInterface)te;
            float renderInputProgress = tile.oldInputProgress + (tile.inputProgress - tile.oldInputProgress) * partialTicks;
            float renderOutputProgress = tile.oldOutputProgress + (tile.outputProgress - tile.oldOutputProgress) * partialTicks;
            renderModel(size, renderInputProgress / TileEntityPressureChamberInterface.MAX_PROGRESS, renderOutputProgress / TileEntityPressureChamberInterface.MAX_PROGRESS);
            if(tile.getStackInSlot(0) != null) {
                GL11.glTranslated(0, 17 / 16F, 0);
                GL11.glScalef(1.0F, -1F, -1F);
                // GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
                boolean fancySetting = RenderManager.instance.options.fancyGraphics;
                RenderManager.instance.options.fancyGraphics = true;
                EntityItem ghostItem = new EntityItem(te.getWorldObj(), 0, 0, 0, tile.getStackInSlot(0));
                ghostItem.hoverStart = 0;
                customRenderItem.doRender(ghostItem, 0, 0, 0, 0, 0);
                RenderManager.instance.options.fancyGraphics = fancySetting;
            }
        } else {
            renderModel(size, 0, 0);
        }
    }

}
