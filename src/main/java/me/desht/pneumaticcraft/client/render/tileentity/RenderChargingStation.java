package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.ChunkPos;

public class RenderChargingStation extends TileEntityRenderer<TileEntityChargingStation> {
    public RenderChargingStation() {
    }

    @Override
    public void render(TileEntityChargingStation te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 0.4, z + 0.5);
        RenderUtils.renderItemAt(te.chargingStackSynced, 0, 0, 0);
        GlStateManager.popMatrix();
    }
}
