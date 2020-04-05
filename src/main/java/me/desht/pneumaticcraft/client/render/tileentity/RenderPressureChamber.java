package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.event.ClientTickHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RenderPressureChamber extends TileEntityRenderer<TileEntityPressureChamberValve> {

    public RenderPressureChamber(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityPressureChamberValve te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
       if (te.multiBlockSize == 0 || !te.hasGlass) return;

        List<ItemStack> stacks = te.renderedItems;
        if (!stacks.isEmpty()){
            double x = te.multiBlockX - te.getPos().getX() + te.multiBlockSize / 2D;
            double y = te.multiBlockY - te.getPos().getY() + 1.1; // Set to '+ 1' for normal y value.
            double z = te.multiBlockZ - te.getPos().getZ() + te.multiBlockSize / 2D;

            matrixStackIn.push();
            matrixStackIn.translate(x, y, z);

            // render single item centered (looks best), multiple items arranged in a circle
            // around the centre of the chamber, radius dependent on chamber size
            float circleRadius = stacks.size() == 1 ? 0 : 0.33f * (te.multiBlockSize - 2);
            float degreesPerStack = 360f / stacks.size();

            // some gentle rotation and bobbing looks good here
            double ticks = ClientTickHandler.TICKS + partialTicks;
            float yBob = MathHelper.sin(((float) ticks  / 10) % 360) * 0.01f;
            float yRot = (float) (ticks / 2) % 360;

            for (int i = 0; i < stacks.size(); i++) {
                matrixStackIn.push();
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(i * degreesPerStack));
                matrixStackIn.translate(circleRadius, yBob, 0);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(yRot));

                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stacks.get(i), te.getWorld(), null);
                itemRenderer.renderItem(stacks.get(i), ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);

                matrixStackIn.pop();
            }
            matrixStackIn.pop();
        }
    }
}
