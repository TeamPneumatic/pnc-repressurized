package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.model.block.ModelChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class RenderChargingStation extends AbstractModelRenderer<TileEntityChargingStation> {
    private final ModelChargingStation model;

    public RenderChargingStation() {
        model = new ModelChargingStation();
    }

    @Override
    ResourceLocation getTexture(TileEntityChargingStation te) {
        return Textures.MODEL_CHARGING_STATION_PAD;
    }

    @Override
    void renderModel(TileEntityChargingStation te, float partialTicks) {
        if (te != null) {
            EntityItem ghostEntityItem = null;
            if (!te.getChargingItem().isEmpty()) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getChargingItem());
            }
            model.renderModel(0.0625f, te.getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) > 0, ghostEntityItem);
        }
    }
}
