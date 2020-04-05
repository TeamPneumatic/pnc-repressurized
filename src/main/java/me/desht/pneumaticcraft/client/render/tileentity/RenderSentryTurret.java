package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderSentryTurret extends TileEntityRenderer<TileEntitySentryTurret> {
    private final ModelDroneMinigun model;

    public RenderSentryTurret(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        model = new ModelDroneMinigun();
    }

    @Override
    public void render(TileEntitySentryTurret te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();

        model.renderMinigun(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.getMinigun(), partialTicks, true);

        matrixStackIn.pop();
    }
}
