package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class RenderChargingStation extends AbstractModelRenderer<TileEntityChargingStation> {
    private RenderEntityItem customRenderItem = null;

    public RenderChargingStation() {
    }

    @Override
    ResourceLocation getTexture(TileEntityChargingStation te) {
        return Textures.MODEL_CHARGING_STATION_PAD;
    }

    @Override
    void renderModel(TileEntityChargingStation te, float partialTicks) {
        if (te != null && !te.chargingStackSynced.isEmpty()) {
            EntityItem ghostEntityItem = new EntityItem(te.getWorld());
            ghostEntityItem.hoverStart = 0.0F;
            ghostEntityItem.setItem(te.chargingStackSynced);
            if (customRenderItem == null) {
                customRenderItem = new NoBobItemRenderer();
            }
            GlStateManager.translate(0, 1.25f, 0);
            GlStateManager.scale(1.0F, -1F, -1F);
            GlStateManager.rotate(90, 0F, 1F, 0F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            renderManager.options.fancyGraphics = fancySetting;
        }
    }
}
