package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ModelAssemblyLaser extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase1;
    private final ModelRenderer armBase2;
    private final ModelRenderer supportMiddle;
    private final ModelRenderer armMiddle1;
    private final ModelRenderer armMiddle2;
    private final ModelRenderer laserBase;
    private final ModelRenderer laser;

    public ModelAssemblyLaser() {
        textureWidth = 64;
        textureHeight = 64;

        baseTurn = new ModelRenderer(this, 0, 17);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        baseTurn.setTextureSize(64, 32);
        baseTurn.mirror = true;
        setRotation(baseTurn, 0F, 0F, 0F);
        baseTurn2 = new ModelRenderer(this, 28, 17);
        baseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        baseTurn2.setRotationPoint(-2F, 17F, -2F);
        baseTurn2.setTextureSize(64, 32);
        baseTurn2.mirror = true;
        setRotation(baseTurn2, 0F, 0F, 0F);
        armBase1 = new ModelRenderer(this, 0, 25);
        armBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase1.setRotationPoint(2F, 17F, -1F);
        armBase1.setTextureSize(64, 32);
        armBase1.mirror = true;
        setRotation(armBase1, 0F, 0F, 0F);
        armBase2 = new ModelRenderer(this, 0, 25);
        armBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase2.setRotationPoint(-3F, 17F, -1F);
        armBase2.setTextureSize(64, 32);
        armBase2.mirror = true;
        setRotation(armBase2, 0F, 0F, 0F);
        supportMiddle = new ModelRenderer(this, 0, 57);
        supportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        supportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        supportMiddle.setTextureSize(64, 32);
        supportMiddle.mirror = true;
        setRotation(supportMiddle, 0F, 0F, 0F);
        armMiddle1 = new ModelRenderer(this, 0, 35);
        armMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle1.setRotationPoint(-2F, 2F, 5F);
        armMiddle1.setTextureSize(64, 32);
        armMiddle1.mirror = true;
        setRotation(armMiddle1, 0F, 0F, 0F);
        armMiddle2 = new ModelRenderer(this, 0, 35);
        armMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle2.setRotationPoint(1F, 2F, 5F);
        armMiddle2.setTextureSize(64, 32);
        armMiddle2.mirror = true;
        setRotation(armMiddle2, 0F, 0F, 0F);
        laserBase = new ModelRenderer(this, 8, 38);
        laserBase.addBox(0F, 0F, 0F, 2, 2, 3);
        laserBase.setRotationPoint(-1F, 2F, 4.5F);
        laserBase.setTextureSize(64, 32);
        laserBase.mirror = true;
        setRotation(laserBase, 0F, 0F, 0F);
        laser = new ModelRenderer(this, 54, 59);
        laser.addBox(0F, 0F, 0F, 1, 1, 32);
        laser.setRotationPoint(-0.5F, 2.5F, 1F);
        laser.setTextureSize(64, 32);
        laser.mirror = true;
        setRotation(laser, 0F, 0F, 0F);
    }

    public void renderModel(float size, float[] angles, boolean laserOn) {
        GlStateManager.pushMatrix();

        GL11.glRotatef(angles[0], 0, 1, 0);
        baseTurn.render(size);
        baseTurn2.render(size);
        GL11.glTranslated(0, 18 / 16F, 0);
        GL11.glRotatef(angles[1], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, 0);
        armBase1.render(size);
        armBase2.render(size);
        supportMiddle.render(size);
        GL11.glTranslated(0, 18 / 16F, 6 / 16F);
        GL11.glRotatef(angles[2], 1, 0, 0);
        GL11.glTranslated(0, -18 / 16F, -6 / 16F);
        armMiddle1.render(size);
        armMiddle2.render(size);
        GL11.glTranslated(0, 3 / 16F, 6 / 16F);
        GL11.glRotatef(angles[3], 1, 0, 0);
        GL11.glTranslated(0, -3 / 16F, -6 / 16F);
        laserBase.render(size);
        if (laserOn) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 2.75 / 16D, 1 / 16D);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4d(1.0D, 0.1D, 0, 1);
            laser.render(size / 8);
            GL11.glPopMatrix();
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        double textSize = 1 / 150D;
        GL11.glScaled(textSize, textSize, textSize);
        GL11.glRotated(-90, 1, 0, 0);
        GL11.glTranslated(0, 0, 18);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiPneumaticContainerBase.drawTexture(Textures.GUI_LASER_DANGER, -8, -65);
        GL11.glEnable(GL11.GL_LIGHTING);

        GlStateManager.popMatrix();
    }
}
