package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderChargingStation extends TileEntityRenderer<TileEntityChargingStation> {
    public RenderChargingStation(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityChargingStation te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.getChargingStackSynced().isEmpty() || !te.getWorld().isAreaLoaded(te.getPos(), 0)) return;

        matrixStackIn.push();

        matrixStackIn.translate(0.5, 0.5, 0.5);
        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.scale(0.5F, 0.5F, 0.5F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(te.getChargingStackSynced(), te.getWorld(), null);
        itemRenderer.renderItem(te.getChargingStackSynced(), ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);

        matrixStackIn.pop();
    }
}
