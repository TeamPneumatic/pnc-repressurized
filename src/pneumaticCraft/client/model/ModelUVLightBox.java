package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityUVLightBox;
import pneumaticCraft.lib.Textures;

public class ModelUVLightBox extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer Top;
    ModelRenderer Support1;
    ModelRenderer Support2;
    ModelRenderer Support3;
    ModelRenderer Support4;
    ModelRenderer BlueprintHolder1;
    ModelRenderer BlueprintHolder2;
    ModelRenderer BlueprintHolder3;
    ModelRenderer BlueprintHolder4;
    ModelRenderer Light11;
    ModelRenderer Light12;
    ModelRenderer Light13;
    ModelRenderer Light14;
    ModelRenderer Light15;
    ModelRenderer Light16;
    ModelRenderer Light21;
    ModelRenderer Light22;
    ModelRenderer Light23;
    ModelRenderer Light24;
    ModelRenderer Light25;
    ModelRenderer Light26;
    ModelRenderer InputLeft1;
    ModelRenderer InputLeft2;
    ModelRenderer InputLeft3;
    ModelRenderer InputLeft4;
    ModelRenderer InputLeft5;

    private final RenderItem customRenderItem;
    private EntityItem blueprintEntity;
    private EntityItem pcbEntity;

    public ModelUVLightBox(){
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

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 14, 1, 7);
        Base.setRotationPoint(-7F, 23F, -3.5F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 0, 0);
        Top.addBox(0F, 0F, 0F, 14, 1, 7);
        Top.setRotationPoint(-7F, 17F, -3.5F);
        Top.setTextureSize(64, 32);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        Support1 = new ModelRenderer(this, 0, 0);
        Support1.addBox(0F, 0F, 0F, 1, 5, 1);
        Support1.setRotationPoint(-7F, 18F, -3.5F);
        Support1.setTextureSize(64, 32);
        Support1.mirror = true;
        setRotation(Support1, 0F, 0F, 0F);
        Support2 = new ModelRenderer(this, 0, 0);
        Support2.addBox(0F, 0F, 0F, 1, 5, 1);
        Support2.setRotationPoint(-7F, 18F, 2.5F);
        Support2.setTextureSize(64, 32);
        Support2.mirror = true;
        setRotation(Support2, 0F, 0F, 0F);
        Support3 = new ModelRenderer(this, 0, 0);
        Support3.addBox(0F, 0F, 0F, 1, 5, 1);
        Support3.setRotationPoint(6F, 18F, -3.5F);
        Support3.setTextureSize(64, 32);
        Support3.mirror = true;
        setRotation(Support3, 0F, 0F, 0F);
        Support4 = new ModelRenderer(this, 0, 0);
        Support4.addBox(0F, 0F, 0F, 1, 5, 1);
        Support4.setRotationPoint(6F, 18F, 2.5F);
        Support4.setTextureSize(64, 32);
        Support4.mirror = true;
        setRotation(Support4, 0F, 0F, 0F);
        BlueprintHolder1 = new ModelRenderer(this, 0, 0);
        BlueprintHolder1.addBox(0F, 0F, 0F, 1, 1, 1);
        BlueprintHolder1.setRotationPoint(-4F, 22F, -2.5F);
        BlueprintHolder1.setTextureSize(64, 32);
        BlueprintHolder1.mirror = true;
        setRotation(BlueprintHolder1, 0F, 0F, 0F);
        BlueprintHolder2 = new ModelRenderer(this, 0, 0);
        BlueprintHolder2.addBox(0F, 0F, 0F, 1, 1, 1);
        BlueprintHolder2.setRotationPoint(-4F, 22F, 1.5F);
        BlueprintHolder2.setTextureSize(64, 32);
        BlueprintHolder2.mirror = true;
        setRotation(BlueprintHolder2, 0F, 0F, 0F);
        BlueprintHolder3 = new ModelRenderer(this, 0, 0);
        BlueprintHolder3.addBox(0F, 0F, 0F, 1, 1, 1);
        BlueprintHolder3.setRotationPoint(3F, 22F, -2.5F);
        BlueprintHolder3.setTextureSize(64, 32);
        BlueprintHolder3.mirror = true;
        setRotation(BlueprintHolder3, 0F, 0F, 0F);
        BlueprintHolder4 = new ModelRenderer(this, 0, 0);
        BlueprintHolder4.addBox(0F, 0F, 0F, 1, 1, 1);
        BlueprintHolder4.setRotationPoint(3F, 22F, 1.5F);
        BlueprintHolder4.setTextureSize(64, 32);
        BlueprintHolder4.mirror = true;
        setRotation(BlueprintHolder4, 0F, 0F, 0F);
        Light11 = new ModelRenderer(this, 0, 26);
        Light11.addBox(0F, 0F, 0F, 1, 1, 5);
        Light11.setRotationPoint(-5.5F, 18F, -2.5F);
        Light11.setTextureSize(64, 32);
        Light11.mirror = true;
        setRotation(Light11, 0F, 0F, 0F);
        Light12 = new ModelRenderer(this, 0, 26);
        Light12.addBox(0F, 0F, 0F, 1, 1, 5);
        Light12.setRotationPoint(-3.5F, 18F, -2.5F);
        Light12.setTextureSize(64, 32);
        Light12.mirror = true;
        setRotation(Light12, 0F, 0F, 0F);
        Light13 = new ModelRenderer(this, 0, 26);
        Light13.addBox(0F, 0F, 0F, 1, 1, 5);
        Light13.setRotationPoint(-1.5F, 18F, -2.5F);
        Light13.setTextureSize(64, 32);
        Light13.mirror = true;
        setRotation(Light13, 0F, 0F, 0F);
        Light14 = new ModelRenderer(this, 0, 26);
        Light14.addBox(0F, 0F, 0F, 1, 1, 5);
        Light14.setRotationPoint(0.5F, 18F, -2.5F);
        Light14.setTextureSize(64, 32);
        Light14.mirror = true;
        setRotation(Light14, 0F, 0F, 0F);
        Light15 = new ModelRenderer(this, 0, 26);
        Light15.addBox(0F, 0F, 0F, 1, 1, 5);
        Light15.setRotationPoint(2.5F, 18F, -2.5F);
        Light15.setTextureSize(64, 32);
        Light15.mirror = true;
        setRotation(Light15, 0F, 0F, 0F);
        Light16 = new ModelRenderer(this, 0, 26);
        Light16.addBox(0F, 0F, 0F, 1, 1, 5);
        Light16.setRotationPoint(4.5F, 18F, -2.5F);
        Light16.setTextureSize(64, 32);
        Light16.mirror = true;
        setRotation(Light16, 0F, 0F, 0F);

        Light21 = new ModelRenderer(this, 12, 26);
        Light21.addBox(0F, 0F, 0F, 1, 1, 5);
        Light21.setRotationPoint(-5.5F, 18F, -2.5F);
        Light21.setTextureSize(64, 32);
        Light21.mirror = true;
        setRotation(Light21, 0F, 0F, 0F);
        Light22 = new ModelRenderer(this, 12, 26);
        Light22.addBox(0F, 0F, 0F, 1, 1, 5);
        Light22.setRotationPoint(-3.5F, 18F, -2.5F);
        Light22.setTextureSize(64, 32);
        Light22.mirror = true;
        setRotation(Light22, 0F, 0F, 0F);
        Light23 = new ModelRenderer(this, 12, 26);
        Light23.addBox(0F, 0F, 0F, 1, 1, 5);
        Light23.setRotationPoint(-1.5F, 18F, -2.5F);
        Light23.setTextureSize(64, 32);
        Light23.mirror = true;
        setRotation(Light23, 0F, 0F, 0F);
        Light24 = new ModelRenderer(this, 12, 26);
        Light24.addBox(0F, 0F, 0F, 1, 1, 5);
        Light24.setRotationPoint(0.5F, 18F, -2.5F);
        Light24.setTextureSize(64, 32);
        Light24.mirror = true;
        setRotation(Light24, 0F, 0F, 0F);
        Light25 = new ModelRenderer(this, 12, 26);
        Light25.addBox(0F, 0F, 0F, 1, 1, 5);
        Light25.setRotationPoint(2.5F, 18F, -2.5F);
        Light25.setTextureSize(64, 32);
        Light25.mirror = true;
        setRotation(Light25, 0F, 0F, 0F);
        Light26 = new ModelRenderer(this, 12, 26);
        Light26.addBox(0F, 0F, 0F, 1, 1, 5);
        Light26.setRotationPoint(4.5F, 18F, -2.5F);
        Light26.setTextureSize(64, 32);
        Light26.mirror = true;
        setRotation(Light26, 0F, 0F, 0F);

        InputLeft1 = new ModelRenderer(this, 0, 12);
        InputLeft1.addBox(0F, 0F, 0F, 1, 1, 2);
        InputLeft1.setRotationPoint(7F, 17F, -1F);
        InputLeft1.setTextureSize(64, 32);
        InputLeft1.mirror = true;
        setRotation(InputLeft1, 0F, 0F, 0F);
        InputLeft2 = new ModelRenderer(this, 0, 12);
        InputLeft2.addBox(0F, 0F, 0F, 3, 2, 1);
        InputLeft2.setRotationPoint(5F, 15F, 1F);
        InputLeft2.setTextureSize(64, 32);
        InputLeft2.mirror = true;
        setRotation(InputLeft2, 0F, 0F, 0F);
        InputLeft3 = new ModelRenderer(this, 0, 12);
        InputLeft3.addBox(0F, 0F, 0F, 3, 2, 1);
        InputLeft3.setRotationPoint(5F, 15F, -2F);
        InputLeft3.setTextureSize(64, 32);
        InputLeft3.mirror = true;
        setRotation(InputLeft3, 0F, 0F, 0F);
        InputLeft4 = new ModelRenderer(this, 0, 12);
        InputLeft4.addBox(0F, 0F, 0F, 3, 1, 2);
        InputLeft4.setRotationPoint(5F, 14F, -1F);
        InputLeft4.setTextureSize(64, 32);
        InputLeft4.mirror = true;
        setRotation(InputLeft4, 0F, 0F, 0F);
        InputLeft5 = new ModelRenderer(this, 0, 12);
        InputLeft5.addBox(0F, 0F, 0F, 1, 2, 2);
        InputLeft5.setRotationPoint(4F, 15F, -1F);
        InputLeft5.setTextureSize(64, 32);
        InputLeft5.mirror = true;
        setRotation(InputLeft5, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        Top.render(f5);
        Support1.render(f5);
        Support2.render(f5);
        Support3.render(f5);
        Support4.render(f5);
        BlueprintHolder1.render(f5);
        BlueprintHolder2.render(f5);
        BlueprintHolder3.render(f5);
        BlueprintHolder4.render(f5);
        Light11.render(f5);
        Light12.render(f5);
        Light13.render(f5);
        Light14.render(f5);
        Light15.render(f5);
        Light16.render(f5);
        InputLeft1.render(f5);
        InputLeft2.render(f5);
        InputLeft3.render(f5);
        InputLeft4.render(f5);
        InputLeft5.render(f5);
    }

    public void renderModel(float size, boolean renderLeft, boolean renderRight, boolean lightsOn){
        Base.render(size);
        Top.render(size);
        Support1.render(size);
        Support2.render(size);
        Support3.render(size);
        Support4.render(size);
        BlueprintHolder1.render(size);
        BlueprintHolder2.render(size);
        BlueprintHolder3.render(size);
        BlueprintHolder4.render(size);
        if(lightsOn) {
            Light21.render(size);
            Light22.render(size);
            Light23.render(size);
            Light24.render(size);
            Light25.render(size);
            Light26.render(size);
        } else {
            Light11.render(size);
            Light12.render(size);
            Light13.render(size);
            Light14.render(size);
            Light15.render(size);
            Light16.render(size);
        }
        if(renderLeft) {
            InputLeft1.render(size);
            InputLeft2.render(size);
            InputLeft3.render(size);
            InputLeft4.render(size);
            InputLeft5.render(size);
        }
        if(renderRight) {
            GL11.glRotated(180, 0, 1, 0);
            InputLeft1.render(size);
            InputLeft2.render(size);
            InputLeft3.render(size);
            InputLeft4.render(size);
            InputLeft5.render(size);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){
        if(te instanceof TileEntityUVLightBox) {
            TileEntityUVLightBox tile = (TileEntityUVLightBox)te;
            renderModel(size, tile.leftConnected, tile.rightConnected, tile.areLightsOn);
            if(blueprintEntity == null) {
                blueprintEntity = new EntityItem(tile.getWorldObj());
                blueprintEntity.setEntityItemStack(new ItemStack(Itemss.PCBBlueprint));
                blueprintEntity.hoverStart = 0.0F;
            }
            if(pcbEntity == null) {
                pcbEntity = new EntityItem(tile.getWorldObj());
                pcbEntity.setEntityItemStack(new ItemStack(Itemss.emptyPCB));
                pcbEntity.hoverStart = 0.0F;
            }
            float scaleFactor = 1.0F;// getGhostItemScaleFactor(tileGlassBell.getStackInSlot(TileGlassBell.DISPLAY_SLOT_INVENTORY_INDEX));
            // float rotationAngle = (float) (720.0 *
            // (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);

            // GL11.glTranslatef(10F / 16F, (float)d1 + 2.0F / 16F, (float)d2 + 0.5F); // size
            GL11.glTranslatef(0, 22.0F / 16F, 2F / 16F); // size
            // translateGhostItemByOrientation(ghostEntityItem.getEntityItem(),
            // x, y, z, tileGlassBell.getOrientation());
            GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
            // GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotated(-90, 1, 0, 0);
            //GL11.glRotated(90, 0, 0, 1);
            boolean fancySetting = RenderManager.instance.options.fancyGraphics;
            RenderManager.instance.options.fancyGraphics = true;
            customRenderItem.doRender(blueprintEntity, 0, 0, 0, 0, 0);
            if(tile.inventory[TileEntityUVLightBox.PCB_INDEX] != null && tile.inventory[TileEntityUVLightBox.PCB_INDEX].getItem() == Itemss.emptyPCB) {
                GL11.glTranslated(0, 0, 0.5 / 16D);
                customRenderItem.doRender(pcbEntity, 0, 0, 0, 0, 0);
            }
            RenderManager.instance.options.fancyGraphics = fancySetting;
        } else {
            renderModel(size, false, false, false);
        }
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_UV_LIGHTBOX;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }

}
