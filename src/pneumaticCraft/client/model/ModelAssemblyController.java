package pneumaticCraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.lib.Textures;

public class ModelAssemblyController extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Base;
    ModelRenderer InputBack1;
    ModelRenderer InputBack2;
    ModelRenderer InputBack3;
    ModelRenderer InputBack4;
    ModelRenderer InputBack5;
    ModelRenderer InputBack6;
    ModelRenderer InputBack7;
    ModelRenderer ScreenLeg;
    ModelRenderer Screen;
    ModelRenderer ScreenLegPart;

    public ModelAssemblyController(){
        textureWidth = 64;
        textureHeight = 32;

        Base = new ModelRenderer(this, 0, 15);
        Base.addBox(0F, 0F, 0F, 16, 1, 16);
        Base.setRotationPoint(-8F, 23F, -8F);
        Base.setTextureSize(64, 32);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        InputBack1 = new ModelRenderer(this, 0, 0);
        InputBack1.addBox(0F, 0F, 0F, 2, 1, 3);
        InputBack1.setRotationPoint(-1F, 14F, 5F);
        InputBack1.setTextureSize(64, 32);
        InputBack1.mirror = true;
        setRotation(InputBack1, 0F, 0F, 0F);
        InputBack2 = new ModelRenderer(this, 0, 0);
        InputBack2.addBox(0F, 0F, 0F, 2, 6, 1);
        InputBack2.setRotationPoint(-1F, 17F, 7F);
        InputBack2.setTextureSize(64, 32);
        InputBack2.mirror = true;
        setRotation(InputBack2, 0F, 0F, 0F);
        InputBack3 = new ModelRenderer(this, 0, 0);
        InputBack3.addBox(0F, 0F, 0F, 1, 2, 3);
        InputBack3.setRotationPoint(1F, 15F, 5F);
        InputBack3.setTextureSize(64, 32);
        InputBack3.mirror = true;
        setRotation(InputBack3, 0F, 0F, 0F);
        InputBack4 = new ModelRenderer(this, 0, 0);
        InputBack4.addBox(0F, 0F, 0F, 1, 2, 3);
        InputBack4.setRotationPoint(-2F, 15F, 5F);
        InputBack4.setTextureSize(64, 32);
        InputBack4.mirror = true;
        setRotation(InputBack4, 0F, 0F, 0F);
        InputBack5 = new ModelRenderer(this, 0, 0);
        InputBack5.addBox(0F, 0F, 0F, 2, 8, 1);
        InputBack5.setRotationPoint(-1F, 15F, 4F);
        InputBack5.setTextureSize(64, 32);
        InputBack5.mirror = true;
        setRotation(InputBack5, 0F, 0F, 0F);
        InputBack6 = new ModelRenderer(this, 0, 0);
        InputBack6.addBox(0F, 0F, 0F, 1, 6, 2);
        InputBack6.setRotationPoint(1F, 17F, 5F);
        InputBack6.setTextureSize(64, 32);
        InputBack6.mirror = true;
        setRotation(InputBack6, 0F, 0F, 0F);
        InputBack7 = new ModelRenderer(this, 0, 0);
        InputBack7.addBox(0F, 0F, 0F, 1, 6, 2);
        InputBack7.setRotationPoint(-2F, 17F, 5F);
        InputBack7.setTextureSize(64, 32);
        InputBack7.mirror = true;
        setRotation(InputBack7, 0F, 0F, 0F);
        ScreenLeg = new ModelRenderer(this, 21, 0);
        ScreenLeg.addBox(0F, 0F, 0F, 2, 12, 2);
        ScreenLeg.setRotationPoint(-1F, 11F, -1F);
        ScreenLeg.setTextureSize(64, 32);
        ScreenLeg.mirror = true;
        setRotation(ScreenLeg, 0F, 0F, 0F);
        Screen = new ModelRenderer(this, 33, 0);
        Screen.addBox(0F, 0F, 0F, 10, 6, 1);
        Screen.setRotationPoint(-5F, 8F, 1F);
        Screen.setTextureSize(64, 32);
        Screen.mirror = true;
        setRotation(Screen, -0.5934119F, 0F, 0F);
        ScreenLegPart = new ModelRenderer(this, 14, 1);
        ScreenLegPart.addBox(0F, 0F, 0F, 2, 1, 1);
        ScreenLegPart.setRotationPoint(-1F, 10F, 0F);
        ScreenLegPart.setTextureSize(64, 32);
        ScreenLegPart.mirror = true;
        setRotation(ScreenLegPart, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        InputBack1.render(f5);
        InputBack2.render(f5);
        InputBack3.render(f5);
        InputBack4.render(f5);
        InputBack5.render(f5);
        InputBack6.render(f5);
        InputBack7.render(f5);
        ScreenLeg.render(f5);
        Screen.render(f5);
        ScreenLegPart.render(f5);
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        if(tile instanceof TileEntityAssemblyController) {
            TileEntityAssemblyController te = (TileEntityAssemblyController)tile;
            renderModel(size, te.sidesConnected, true, te.displayedText, te.hasProblem);
        } else {
            renderModel(size, new boolean[6], false, "", false);
        }
    }

    public void renderModel(float size, boolean[] connectedSides, boolean shouldFacePlayer, String displayedText, boolean hasProblem){
        Base.render(size);
        GL11.glPushMatrix();
        if(connectedSides[ForgeDirection.NORTH.ordinal()]) {
            InputBack1.render(size);
            InputBack2.render(size);
            InputBack3.render(size);
            InputBack4.render(size);
            InputBack5.render(size);
            InputBack6.render(size);
            InputBack7.render(size);
        }
        GL11.glRotated(90, 0, 1, 0);
        if(connectedSides[ForgeDirection.EAST.ordinal()]) {
            InputBack1.render(size);
            InputBack2.render(size);
            InputBack3.render(size);
            InputBack4.render(size);
            InputBack5.render(size);
            InputBack6.render(size);
            InputBack7.render(size);
        }
        GL11.glRotated(90, 0, 1, 0);
        if(connectedSides[ForgeDirection.SOUTH.ordinal()]) {
            InputBack1.render(size);
            InputBack2.render(size);
            InputBack3.render(size);
            InputBack4.render(size);
            InputBack5.render(size);
            InputBack6.render(size);
            InputBack7.render(size);
        }
        GL11.glRotated(90, 0, 1, 0);
        if(connectedSides[ForgeDirection.WEST.ordinal()]) {
            InputBack1.render(size);
            InputBack2.render(size);
            InputBack3.render(size);
            InputBack4.render(size);
            InputBack5.render(size);
            InputBack6.render(size);
            InputBack7.render(size);
        }
        GL11.glPopMatrix();
        if(shouldFacePlayer) GL11.glRotatef(180 + RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        ScreenLeg.render(size);
        Screen.render(size);
        ScreenLegPart.render(size);
        double textSize = 1 / 100D;
        GL11.glTranslated(-0.25D, 0.53D, 0.04D);
        GL11.glRotated(-34, 1, 0, 0);
        GL11.glScaled(textSize, textSize, textSize);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().fontRenderer.drawString(displayedText, 1, 4, 0xFFFFFFFF);
        if(hasProblem) GuiPneumaticContainerBase.drawTexture(Textures.GUI_PROBLEMS_TEXTURE, 28, 12);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_ASSEMBLY_CONTROLLER;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }

}
