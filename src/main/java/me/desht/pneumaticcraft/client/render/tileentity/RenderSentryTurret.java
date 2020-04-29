package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class RenderSentryTurret extends AbstractTileModelRenderer<TileEntitySentryTurret> {
    private final ModelMinigun model;

    public RenderSentryTurret(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        model = new ModelMinigun();
    }

    @Override
    void renderModel(TileEntitySentryTurret te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.translate(0, -13 / 16F, 0);
        model.renderMinigun(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.getMinigun(), partialTicks, false);
    }

    @Override
    protected void renderExtras(TileEntitySentryTurret te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (RenderMinigunTracers.shouldRender(te.getMinigun())) {
            matrixStack.push();
            matrixStack.translate(0.5, 0.75, 0.5);
            BlockPos pos = te.getPos();
            RenderMinigunTracers.render(te.getMinigun(), matrixStack, bufferIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            matrixStack.pop();
        }
    }
}
