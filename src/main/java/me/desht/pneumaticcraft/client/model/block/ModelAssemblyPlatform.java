package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.api.client.assemblymachine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;

public class ModelAssemblyPlatform extends ModelBase {
    private final ModelRenderer claw1;
    private final ModelRenderer claw2;
    private RenderEntityItem customRenderItem = null;

    public ModelAssemblyPlatform() {
        textureWidth = 64;
        textureHeight = 64;

        claw1 = new ModelRenderer(this, 0, 32);
        claw1.addBox(0F, 0F, 0F, 2, 1, 1);
        claw1.setRotationPoint(-1F, 17F, 0F);
        claw1.setTextureSize(64, 32);
        claw1.mirror = true;
        setRotation(claw1, 0F, 0F, 0F);
        claw2 = new ModelRenderer(this, 0, 32);
        claw2.addBox(0F, 0F, 0F, 2, 1, 1);
        claw2.setRotationPoint(-1F, 17F, -1F);
        claw2.setTextureSize(64, 32);
        claw2.mirror = true;
        setRotation(claw2, 0F, 0F, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void renderModel(float size, float progress, EntityItem carriedItem) {
        float clawTrans;
        float scaleFactor = 0.7F;

        if (customRenderItem == null) {
            customRenderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
                @Override
                public boolean shouldBob() {
                    return false;
                }
            };
        }

        IAssemblyRenderOverriding renderOverride = null;
        if (carriedItem != null) {
            renderOverride = GuiRegistry.renderOverrides.get(carriedItem.getItem().getItem().getRegistryName());
            if(renderOverride != null) {
                clawTrans = renderOverride.getPlatformClawShift(carriedItem.getItem());
            } else {
                if (carriedItem.getItem().getItem() instanceof ItemBlock) {
                    clawTrans = 1.5F / 16F - progress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - progress * 1.4F / 16F;
//                    scaleFactor = 0.6F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - progress * 1.5F / 16F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, clawTrans);
        claw1.render(size);
        GlStateManager.translate(0, 0, -2 * clawTrans);
        claw2.render(size);
        GlStateManager.popMatrix();

        if (carriedItem != null) {
            if (renderOverride == null || renderOverride.applyRenderChangePlatform(carriedItem.getItem())) {
                GlStateManager.rotate(180, 1, 0, 0);
                double yOffset = carriedItem.getItem().getItem() instanceof ItemBlock ? -16.5 / 16F : -17.5 / 16F;
                GlStateManager.translate(0, yOffset - 0.2, 0);
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                customRenderItem.doRender(carriedItem, 0, 0, 0, 0, 0);
            }
        }
    }
}
