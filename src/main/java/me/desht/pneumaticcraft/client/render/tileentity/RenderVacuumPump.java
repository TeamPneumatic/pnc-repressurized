package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderVacuumPump extends AbstractTileModelRenderer<TileEntityVacuumPump> {
    private static final int BLADE_COUNT = 6;

    private final ModelRenderer blade;

    public RenderVacuumPump(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        blade = new ModelRenderer(32, 32, 24, 25);
        blade.addBox(0.0F, 0.0F, -1.0F, 1.0F, 4.0F, 3.0F);
        blade.setPos(-0.5F, 14.0F, -3.0F);
        blade.mirror = true;
    }

    @Override
    public void renderModel(TileEntityVacuumPump te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityTranslucent(Textures.MODEL_VACUUM_PUMP));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90));

        renderBlades(te, partialTicks, matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }


    private void renderBlades(TileEntityVacuumPump te, float partialTicks, MatrixStack matrixStackIn, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn) {
        float rotation = MathHelper.lerp(partialTicks, te.oldRotation, te.rotation) + 1;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, -0.68f, 1f);
        matrixStackIn.scale(0.8f, 0.8f, 0.8f);
        for (int i = 0; i < BLADE_COUNT; i++) {
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotation * 2 + (i + 0.5F) / BLADE_COUNT * 360));
            matrixStackIn.translate(0, 0, 1D / 16D);
            blade.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180));
    }
}
