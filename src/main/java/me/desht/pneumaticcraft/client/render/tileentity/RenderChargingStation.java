package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.ChunkPos;

public class RenderChargingStation extends TileEntityRenderer<TileEntityChargingStation> {
    public RenderChargingStation() {
    }

    @Override
    public void render(TileEntityChargingStation te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;
        if (te.chargingStackEntity == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);
        RenderUtils.rotateMatrixForDirection(te.getRotation());
        GlStateManager.scalef(0.5F, 0.5F, 0.5F);
        Minecraft.getInstance().getItemRenderer().renderItem(te.chargingStackEntity.getItem(), ItemCameraTransforms.TransformType.FIXED);

        GlStateManager.popMatrix();
    }
}
