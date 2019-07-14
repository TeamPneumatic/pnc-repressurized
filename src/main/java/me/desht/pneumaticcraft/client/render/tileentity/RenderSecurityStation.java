package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class RenderSecurityStation extends TileEntityRenderer<TileEntitySecurityStation> {
    @Override
    public void render(TileEntitySecurityStation te, double x, double y, double z, float partialTicks, int destroyStage) {
        te.renderRangeLines();
    }
}
