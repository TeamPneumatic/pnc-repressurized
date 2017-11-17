package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.api.client.assemblymachine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import org.lwjgl.opengl.GL11;

public class ModelAssemblyIOUnit extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase1;
    private final ModelRenderer armBase2;
    private final ModelRenderer supportMiddle;
    private final ModelRenderer armMiddle1;
    private final ModelRenderer armMiddle2;
    private final ModelRenderer clawBase;
    private final ModelRenderer clawAxil;
    private final ModelRenderer clawTurn;
    private final ModelRenderer claw1;
    private final ModelRenderer claw2;
    private RenderEntityItem customRenderer = null;

    public ModelAssemblyIOUnit() {
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
        clawBase = new ModelRenderer(this, 8, 38);
        clawBase.addBox(0F, 0F, 0F, 2, 2, 3);
        clawBase.setRotationPoint(-1F, 2F, 4.5F);
        clawBase.setTextureSize(64, 32);
        clawBase.mirror = true;
        setRotation(clawBase, 0F, 0F, 0F);
        clawAxil = new ModelRenderer(this, 8, 45);
        clawAxil.addBox(0F, 0F, 0F, 1, 1, 1);
        clawAxil.setRotationPoint(-0.5F, 2.5F, 4F);
        clawAxil.setTextureSize(64, 32);
        clawAxil.mirror = true;
        setRotation(clawAxil, 0F, 0F, 0F);
        clawTurn = new ModelRenderer(this, 8, 49);
        clawTurn.addBox(0F, 0F, 0F, 4, 2, 1);
        clawTurn.setRotationPoint(-2F, 2F, 3F);
        clawTurn.setTextureSize(64, 32);
        clawTurn.mirror = true;
        setRotation(clawTurn, 0F, 0F, 0F);
        claw1 = new ModelRenderer(this, 8, 54);
        claw1.addBox(0F, 0F, 0F, 1, 2, 1);
        claw1.setRotationPoint(0F, 2F, 2F);
        claw1.setTextureSize(64, 32);
        claw1.mirror = true;
        setRotation(claw1, 0F, 0F, 0F);
        claw2 = new ModelRenderer(this, 8, 59);
        claw2.addBox(0F, 0F, 0F, 1, 2, 1);
        claw2.setRotationPoint(-1F, 2F, 2F);
        claw2.setTextureSize(64, 32);
        claw2.mirror = true;
        setRotation(claw2, 0F, 0F, 0F);
    }

    public void renderModel(float size, float[] angles, float clawProgress, EntityItem carriedItem) {
        float clawTrans;
        float scaleFactor = 0.7F;

        if (customRenderer == null) {
            customRenderer = new AbstractModelRenderer.NoBobItemRenderer();
        }

        IAssemblyRenderOverriding renderOverride = null;
        if (carriedItem != null) {
            renderOverride = GuiRegistry.renderOverrides.get(carriedItem.getItem().getItem().getRegistryName());
            if (renderOverride != null) {
                clawTrans = renderOverride.getIOUnitClawShift(carriedItem.getItem());
            } else {
                if (carriedItem.getItem().getItem() instanceof ItemBlock) {
                    clawTrans = 1.5F / 16F - clawProgress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - clawProgress * 1.4F / 16F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - clawProgress * 1.5F / 16F;
        }

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
        clawBase.render(size);
        GL11.glTranslated(0, 3 / 16F, 0);
        GL11.glRotatef(angles[4], 0, 0, 1);
        GL11.glTranslated(0, -3 / 16F, 0);
        clawAxil.render(size);
        clawTurn.render(size);

        GlStateManager.pushMatrix();
        GL11.glTranslated(clawTrans, 0, 0);
        claw1.render(size);
        GL11.glTranslated(-2 * clawTrans, 0, 0);
        claw2.render(size);
        GlStateManager.popMatrix();

        if (carriedItem != null) {
            if (renderOverride == null || renderOverride.applyRenderChangeIOUnit(carriedItem.getItem())) {
                GlStateManager.rotate(90, 1, 0, 0);
                double yOffset = carriedItem.getItem().getItem() instanceof ItemBlock ? 1.5 / 16D : 0.5 / 16D;
                GlStateManager.translate(0, yOffset - 0.2, -3 / 16D);
                GlStateManager.rotate(-90, 0, 1, 0);
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                customRenderer.doRender(carriedItem, 0, 0, 0, 0, 0);
            }
        }

        GlStateManager.popMatrix();
    }
}
