package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderSecurityStation extends TileEntitySpecialRenderer<TileEntitySecurityStation> {
    @Override
    public void render(TileEntitySecurityStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        te.renderRangeLines();
    }
}
