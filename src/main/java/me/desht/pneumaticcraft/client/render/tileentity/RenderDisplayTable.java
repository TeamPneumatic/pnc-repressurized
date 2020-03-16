package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;

public class RenderDisplayTable extends TileEntityRenderer<TileEntityDisplayTable> {
    @Override
    public void render(TileEntityDisplayTable te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;
        ItemStack stack = new ItemStack(Item.getItemById(te.itemId));

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 1, z + 0.5);
        renderItemAt(stack, 0, 0, 0.5, te.getRotation());
        GlStateManager.popMatrix();
    }

    static void renderItemAt(ItemStack stack, double itemX, double itemZ, double scale, Direction rot) {
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            RenderUtils.rotateMatrixForDirection(rot);
            if (stack.getItem() instanceof BlockItem) {
                GlStateManager.translated(itemX, scale / 4d, itemZ);
            } else {
                // lie items flat
                GlStateManager.translated(itemX, 0.025, itemZ);
                GlStateManager.rotated(90, 1, 0, 0);
            }
            GlStateManager.scaled(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }
}
