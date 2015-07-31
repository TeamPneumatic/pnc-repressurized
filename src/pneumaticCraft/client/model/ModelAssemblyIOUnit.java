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
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class ModelAssemblyIOUnit extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer BaseTurn;
    ModelRenderer BaseTurn2;
    ModelRenderer ArmBase1;
    ModelRenderer ArmBase2;
    ModelRenderer SupportMiddle;
    ModelRenderer ArmMiddle1;
    ModelRenderer ArmMiddle2;
    ModelRenderer ClawBase;
    ModelRenderer ClawAxil;
    ModelRenderer ClawTurn;
    ModelRenderer Claw1;
    ModelRenderer Claw2;
    private final RenderItem customRenderItem;

    public ModelAssemblyIOUnit(){
        // EE3 snippet, to initialize an EntityItem which doesn't bob.
        customRenderItem = new RenderItem(){
            @Override
            public boolean shouldBob(){

                return false;
            };
        };
        customRenderItem.setRenderManager(RenderManager.instance);

        textureWidth = 64;
        textureHeight = 64;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 1F, 16, 1, 16);
        Base.setRotationPoint(-8F, 23F, -9F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        BaseTurn = new ModelRenderer(this, 0, 17);
        BaseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        BaseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        BaseTurn.setTextureSize(64, 32);
        BaseTurn.mirror = true;
        setRotation(BaseTurn, 0F, 0F, 0F);
        BaseTurn2 = new ModelRenderer(this, 28, 17);
        BaseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        BaseTurn2.setRotationPoint(-2F, 17F, -2F);
        BaseTurn2.setTextureSize(64, 32);
        BaseTurn2.mirror = true;
        setRotation(BaseTurn2, 0F, 0F, 0F);
        ArmBase1 = new ModelRenderer(this, 0, 25);
        ArmBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        ArmBase1.setRotationPoint(2F, 17F, -1F);
        ArmBase1.setTextureSize(64, 32);
        ArmBase1.mirror = true;
        setRotation(ArmBase1, 0F, 0F, 0F);
        ArmBase2 = new ModelRenderer(this, 0, 25);
        ArmBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        ArmBase2.setRotationPoint(-3F, 17F, -1F);
        ArmBase2.setTextureSize(64, 32);
        ArmBase2.mirror = true;
        setRotation(ArmBase2, 0F, 0F, 0F);
        SupportMiddle = new ModelRenderer(this, 0, 57);
        SupportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        SupportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        SupportMiddle.setTextureSize(64, 32);
        SupportMiddle.mirror = true;
        setRotation(SupportMiddle, 0F, 0F, 0F);
        ArmMiddle1 = new ModelRenderer(this, 0, 35);
        ArmMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        ArmMiddle1.setRotationPoint(-2F, 2F, 5F);
        ArmMiddle1.setTextureSize(64, 32);
        ArmMiddle1.mirror = true;
        setRotation(ArmMiddle1, 0F, 0F, 0F);
        ArmMiddle2 = new ModelRenderer(this, 0, 35);
        ArmMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        ArmMiddle2.setRotationPoint(1F, 2F, 5F);
        ArmMiddle2.setTextureSize(64, 32);
        ArmMiddle2.mirror = true;
        setRotation(ArmMiddle2, 0F, 0F, 0F);
        ClawBase = new ModelRenderer(this, 8, 38);
        ClawBase.addBox(0F, 0F, 0F, 2, 2, 3);
        ClawBase.setRotationPoint(-1F, 2F, 4.5F);
        ClawBase.setTextureSize(64, 32);
        ClawBase.mirror = true;
        setRotation(ClawBase, 0F, 0F, 0F);
        ClawAxil = new ModelRenderer(this, 8, 45);
        ClawAxil.addBox(0F, 0F, 0F, 1, 1, 1);
        ClawAxil.setRotationPoint(-0.5F, 2.5F, 4F);
        ClawAxil.setTextureSize(64, 32);
        ClawAxil.mirror = true;
        setRotation(ClawAxil, 0F, 0F, 0F);
        ClawTurn = new ModelRenderer(this, 8, 49);
        ClawTurn.addBox(0F, 0F, 0F, 4, 2, 1);
        ClawTurn.setRotationPoint(-2F, 2F, 3F);
        ClawTurn.setTextureSize(64, 32);
        ClawTurn.mirror = true;
        setRotation(ClawTurn, 0F, 0F, 0F);
        Claw1 = new ModelRenderer(this, 8, 54);
        Claw1.addBox(0F, 0F, 0F, 1, 2, 1);
        Claw1.setRotationPoint(0F, 2F, 2F);
        Claw1.setTextureSize(64, 32);
        Claw1.mirror = true;
        setRotation(Claw1, 0F, 0F, 0F);
        Claw2 = new ModelRenderer(this, 8, 59);
        Claw2.addBox(0F, 0F, 0F, 1, 2, 1);
        Claw2.setRotationPoint(-1F, 2F, 2F);
        Claw2.setTextureSize(64, 32);
        Claw2.mirror = true;
        setRotation(Claw2, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        BaseTurn.render(f5);
        BaseTurn2.render(f5);
        ArmBase1.render(f5);
        ArmBase2.render(f5);
        SupportMiddle.render(f5);
        ArmMiddle1.render(f5);
        ArmMiddle2.render(f5);
        ClawBase.render(f5);
        ClawAxil.render(f5);
        ClawTurn.render(f5);
        Claw1.render(f5);
        Claw2.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(te != null && te.getBlockMetadata() == 0 ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT);
        if(te instanceof TileEntityAssemblyIOUnit) {
            TileEntityAssemblyIOUnit tile = (TileEntityAssemblyIOUnit)te;
            float[] renderAngles = new float[5];
            for(int i = 0; i < 5; i++) {
                renderAngles[i] = tile.oldAngles[i] + (tile.angles[i] - tile.oldAngles[i]) * partialTicks;
            }
            // float rotationAngle = (float) (720.0 *
            // (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);

            EntityItem ghostEntityItem = null;
            if(tile.inventory[0] != null) {
                ghostEntityItem = new EntityItem(tile.getWorldObj());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setEntityItemStack(tile.inventory[0]);
            }
            boolean fancySetting = RenderManager.instance.options.fancyGraphics;
            RenderManager.instance.options.fancyGraphics = true;
            renderModel(size, renderAngles, tile.oldClawProgress + (tile.clawProgress - tile.oldClawProgress) * partialTicks, ghostEntityItem);
            RenderManager.instance.options.fancyGraphics = fancySetting;
        } else {
            renderModel(size, new float[]{0, 0, 35, 55, 0}, 0, null);
        }
    }

    public void renderModel(float size, float[] angles, float clawProgress, EntityItem carriedItem){
        float clawTrans;
        float scaleFactor = 0.7F;

        IAssemblyRenderOverriding renderOverride = null;
        if(carriedItem != null) {
            renderOverride = AssemblyRenderOverriding.renderOverrides.get(carriedItem.getEntityItem());
            if(renderOverride != null) {
                clawTrans = renderOverride.getIOUnitClawShift(carriedItem.getEntityItem());
            } else {
                if(carriedItem.getEntityItem().getItem() instanceof ItemBlock) {
                    clawTrans = 1.5F / 16F - clawProgress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - clawProgress * 1.4F / 16F;
                    scaleFactor = 0.4F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - clawProgress * 1.5F / 16F;
        }
        Base.render(size);
        GL11.glPushMatrix();
        GL11.glRotatef(angles[0], 0, 1, 0);
        BaseTurn.render(size);
        BaseTurn2.render(size);
        GL11.glTranslated(0, 18 / 16F, 0);
        GL11.glRotatef(angles[1], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, 0);
        ArmBase1.render(size);
        ArmBase2.render(size);
        SupportMiddle.render(size);
        GL11.glTranslated(0, 18 / 16F, 6 / 16F);
        GL11.glRotatef(angles[2], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, -6 / 16F);
        ArmMiddle1.render(size);
        ArmMiddle2.render(size);
        GL11.glTranslated(0, 3 / 16F, 6 / 16F);
        GL11.glRotatef(angles[3], 1, 0, 0);
        GL11.glTranslated(0, -3 / 16F, -6 / 16F);
        ClawBase.render(size);
        GL11.glTranslated(0, 3 / 16F, 0);
        GL11.glRotatef(angles[4], 0, 0, 1);
        GL11.glTranslated(0, -3 / 16F, 0);
        ClawAxil.render(size);
        ClawTurn.render(size);
        GL11.glPushMatrix();
        GL11.glTranslated(clawTrans, 0, 0);
        Claw1.render(size);
        GL11.glTranslated(-2 * clawTrans, 0, 0);
        Claw2.render(size);
        GL11.glPopMatrix();

        if(carriedItem != null) {
            if(renderOverride == null || renderOverride.applyRenderChangeIOUnit(carriedItem.getEntityItem())) {
                GL11.glRotated(90, 1, 0, 0);
                GL11.glTranslated(0, carriedItem.getEntityItem().getItem() instanceof ItemBlock ? 1.5 / 16D : 0.5 / 16D, -3 / 16D);
                GL11.glRotated(-90, 0, 1, 0);

                GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
                customRenderItem.doRender(carriedItem, 0, 0, 0, 0, 0);
            }
        }

        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return null;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
