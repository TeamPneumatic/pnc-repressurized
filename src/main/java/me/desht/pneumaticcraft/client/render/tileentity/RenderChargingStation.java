package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.StaticItemRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.ChunkPos;

public class RenderChargingStation extends TileEntityRenderer<TileEntityChargingStation> {
    private StaticItemRenderer customRenderItem = null;

    public RenderChargingStation() {
    }

    @Override
    public void render(TileEntityChargingStation te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;
        if (te.chargingStackEntity == null) return;

        if (customRenderItem == null) {
            customRenderItem = new StaticItemRenderer();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 0.4, z + 0.5);
        double rot = 0;
        switch (te.getRotation()) {
            case NORTH: rot = 180; break;
            case EAST: rot = 90; break;
            case WEST: rot = -90; break;
        }
        if (rot != 0) {
            GlStateManager.rotated(rot, 0, 1, 0);
        }
        EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        boolean fancySetting = renderManager.options.fancyGraphics;
        renderManager.options.fancyGraphics = true;
        customRenderItem.doRender(te.chargingStackEntity, 0, 0, 0, 0, 0);
        renderManager.options.fancyGraphics = fancySetting;
        GlStateManager.popMatrix();
    }
}
