package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTagWorkbench;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;

public class RenderTagWorkbench extends TileEntityRenderer<TileEntityTagWorkbench> {
    @Override
    public void render(TileEntityTagWorkbench te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 1, z + 0.5);
        RenderDisplayTable.renderItemAt(new ItemStack(Item.getItemById(te.itemId)), 0, -0.25, 0.4, te.getRotation());
        RenderDisplayTable.renderItemAt(new ItemStack(Item.getItemById(te.paperItemId)), -0.25, 0.25, 0.4, te.getRotation());
        RenderDisplayTable.renderItemAt(new ItemStack(Item.getItemById(te.outputItemId)), 0.25, 0.25, 0.4, te.getRotation());
        GlStateManager.popMatrix();
    }
}
