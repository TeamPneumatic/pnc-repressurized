package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ModelAssemblyPlatform extends AbstractTileModelRenderer.BaseModel {
    private static final float ITEM_SCALE = 0.5F;

    private final RendererModel claw1;
    private final RendererModel claw2;

    public ModelAssemblyPlatform() {
        textureWidth = 64;
        textureHeight = 64;

        claw1 = new RendererModel(this, 0, 32);
        claw1.addBox(0F, 0F, 0F, 2, 1, 1);
        claw1.setRotationPoint(-1F, 17F, 0F);
        claw1.setTextureSize(64, 32);
        claw1.mirror = true;
        setRotation(claw1, 0F, 0F, 0F);
        claw2 = new RendererModel(this, 0, 32);
        claw2.addBox(0F, 0F, 0F, 2, 1, 1);
        claw2.setRotationPoint(-1F, 17F, -1F);
        claw2.setTextureSize(64, 32);
        claw2.mirror = true;
        setRotation(claw2, 0F, 0F, 0F);
    }

    public void renderModel(float size, float progress, ItemStack carriedItem) {
        float clawTrans;

        IAssemblyRenderOverriding renderOverride = null;
        if (!carriedItem.isEmpty()) {
            renderOverride = GuiRegistry.renderOverrides.get(carriedItem.getItem().getItem().getRegistryName());
            if(renderOverride != null) {
                clawTrans = renderOverride.getPlatformClawShift(carriedItem);
            } else {
                if (carriedItem.getItem().getItem() instanceof BlockItem) {
                    clawTrans = 1.5F / 16F - progress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - progress * 1.4F / 16F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - progress * 1.5F / 16F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0, clawTrans);
        claw1.render(size);
        GlStateManager.translated(0, 0, -2 * clawTrans);
        claw2.render(size);
        GlStateManager.popMatrix();

        if (!carriedItem.isEmpty()) {
            if (renderOverride == null || renderOverride.applyRenderChangePlatform(carriedItem)) {
                GlStateManager.rotated(180, 1, 0, 0);
                double yOffset = carriedItem.getItem().getItem() instanceof BlockItem ? -16.5 / 16F : -17.5 / 16F;
                GlStateManager.translated(0, yOffset + 0.05, 0);
                GlStateManager.scaled(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                Minecraft.getInstance().getItemRenderer().renderItem(carriedItem, ItemCameraTransforms.TransformType.FIXED);
            }
        }
    }
}
