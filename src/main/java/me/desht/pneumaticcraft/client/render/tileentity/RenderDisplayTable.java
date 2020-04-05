package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;

public class RenderDisplayTable extends TileEntityRenderer<TileEntityDisplayTable> {
    public RenderDisplayTable(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityDisplayTable te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;
        ItemStack stack = new ItemStack(Item.getItemById(te.itemId));

        matrixStackIn.push();
        matrixStackIn.translate(0.5, 1, 0.5);
        renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, stack, 0, 0, 0.5f, te.getRotation());
        matrixStackIn.pop();
    }

    static void renderItemAt(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, double xOffset, double zOffset, float scale, Direction rot) {
        if (!stack.isEmpty()) {
            matrixStackIn.push();
            RenderUtils.rotateMatrixForDirection(matrixStackIn, rot);
            if (stack.getItem() instanceof BlockItem) {
                matrixStackIn.translate(xOffset, scale / 4d, zOffset);
            } else {
                // lie items flat
                matrixStackIn.translate(xOffset, 0.025, zOffset);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
            }
            matrixStackIn.scale(scale, scale, scale);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);
            matrixStackIn.pop();
        }
    }
}
