package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTagWorkbench;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;

public class RenderTagWorkbench extends TileEntityRenderer<TileEntityTagWorkbench> {
    public RenderTagWorkbench(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityTagWorkbench te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getLevel().getChunkSource().isEntityTickingChunk(new ChunkPos(te.getBlockPos()))) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1, 0.5);
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.itemId)), 0, -0.25, 0.4f, te.getRotation());
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.paperItemId)), -0.25, 0.25, 0.4f, te.getRotation());
        RenderDisplayTable.renderItemAt(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                new ItemStack(Item.byId(te.outputItemId)), 0.25, 0.25, 0.4f, te.getRotation());
        matrixStackIn.popPose();
    }
}
