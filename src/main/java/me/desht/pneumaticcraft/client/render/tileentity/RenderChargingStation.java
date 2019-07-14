package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.ResourceLocation;

public class RenderChargingStation extends AbstractModelRenderer<TileEntityChargingStation> {
    private ItemRenderer customRenderItem = null;

    public RenderChargingStation() {
    }

    @Override
    ResourceLocation getTexture(TileEntityChargingStation te) {
        return Textures.MODEL_CHARGING_STATION_PAD;
    }

    @Override
    void renderModel(TileEntityChargingStation te, float partialTicks) {
        if (te != null && !te.chargingStackSynced.isEmpty()) {
            ItemEntity ghostEntityItem = new ItemEntity(te.getWorld());
            ghostEntityItem.hoverStart = 0.0F;
            ghostEntityItem.setItem(te.chargingStackSynced);
            if (customRenderItem == null) {
                customRenderItem = new NoBobItemRenderer();
            }
            GlStateManager.translated(0, 1.25f, 0);
            GlStateManager.scaled(1.0F, -1F, -1F);
            GlStateManager.rotated(90, 0F, 1F, 0F);

            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            renderManager.options.fancyGraphics = fancySetting;
        }
    }
}
