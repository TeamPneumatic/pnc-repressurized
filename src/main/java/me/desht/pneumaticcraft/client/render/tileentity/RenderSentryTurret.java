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

        if (RenderMinigunTracers.shouldRender(te.getMinigun())) {
            matrixStackIn.push();
            matrixStackIn.scale(1.0F, -1, -1F);
            matrixStackIn.translate(0, -1.45, 0);
            BlockPos pos = te.getPos();
            RenderMinigunTracers.render(te.getMinigun(), matrixStackIn, bufferIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            matrixStackIn.pop();
        }
    }
}
