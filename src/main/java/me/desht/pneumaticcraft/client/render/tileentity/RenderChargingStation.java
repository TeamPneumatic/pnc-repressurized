package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.model.block.ModelChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;

public class RenderChargingStation extends TileEntitySpecialRenderer<TileEntityChargingStation> {
    private final ModelChargingStation model;

    public RenderChargingStation() {
        model = new ModelChargingStation();
    }

    @Override
    public void render(TileEntityChargingStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        if (te != null) {
            EntityItem ghostEntityItem = null;
            if (!te.getChargingItem().isEmpty()) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getChargingItem());
            }
            model.renderModel(0.0625f, te.getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) > 0, ghostEntityItem);
        }

        GlStateManager.popMatrix();
    }
}
