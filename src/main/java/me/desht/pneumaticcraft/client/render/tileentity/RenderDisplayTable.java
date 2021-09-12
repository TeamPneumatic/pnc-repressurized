package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockDisplayShelf;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3f;

public class RenderDisplayTable extends TileEntityRenderer<TileEntityDisplayTable> {
    public RenderDisplayTable(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityDisplayTable te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getLevel().getChunkSource().isEntityTickingChunk(new ChunkPos(te.getBlockPos()))) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1, 0.5);
        Block b = te.getBlockState().getBlock();
        double yOff = b instanceof BlockDisplayShelf ? 1d - (((BlockDisplayShelf) b).getTableHeight()): 0d;
        renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.displayedStack, 0d, yOff, 0d, 0.5f, te.getRotation());
        matrixStackIn.popPose();
    }

    static void renderItemAt(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, double xOffset, double yOffset, double zOffset, float scale, Direction rot) {
        if (!stack.isEmpty()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0, -yOffset, 0);
            RenderUtils.rotateMatrixForDirection(matrixStackIn, rot);
            if (stack.getItem() instanceof BlockItem) {
                matrixStackIn.translate(xOffset, scale / 4d, zOffset);
            } else {
                // lie items flat
                matrixStackIn.translate(xOffset, 0.025, zOffset);
                matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90));
            }
            matrixStackIn.scale(scale, scale, scale);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            IBakedModel ibakedmodel = itemRenderer.getModel(stack, Minecraft.getInstance().level, null);
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);
            matrixStackIn.popPose();
        }
    }
}
