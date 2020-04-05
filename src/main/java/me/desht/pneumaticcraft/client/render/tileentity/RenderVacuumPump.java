package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;

public class RenderVacuumPump extends TileEntityRenderer<TileEntityVacuumPump> {
    private static final int BLADE_COUNT = 3;
    private static final int CASE_POINTS = 20;

    private final ModelRenderer turbineCase;
    private final ModelRenderer top;
    private final ModelRenderer blade;

    public RenderVacuumPump(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        turbineCase = new ModelRenderer(64, 64, 0, 47);
        turbineCase.addBox(0F, 0F, 0F, 1, 4, 1);
        turbineCase.setRotationPoint(-0.5F, 14.1F, 0F);
        turbineCase.mirror = true;
        top = new ModelRenderer(64, 64, 0, 47);
        top.addBox(0F, 0F, 0F, 6, 1, 12);
        top.setRotationPoint(-3F, 13F, -6F);
        top.mirror = true;
        blade = new ModelRenderer(64, 64, 0, 0);
        blade.addBox(0F, 0F, 0F, 1, 4, 2);
        blade.setRotationPoint(-0.5F, 14F, -3F);
        blade.mirror = true;
    }

    @Override
    public void render(TileEntityVacuumPump te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityTranslucent(Textures.MODEL_VACUUM_PUMP));

        matrixStackIn.push();

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));

        renderBlades(te, partialTicks, matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        renderCase(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        renderPlusAndMinus(matrixStackIn, bufferIn);

        matrixStackIn.pop();
    }

    private static final float PLUS_MINUS_SCALE = 0.05F;

    private void renderBlades(TileEntityVacuumPump te, float partialTicks, MatrixStack matrixStackIn, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn) {
        float rotation = MathHelper.lerp(partialTicks, te.oldRotation, te.rotation) + 1;

        matrixStackIn.push();
        matrixStackIn.translate(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotation * 2 + (i + 0.5F) / BLADE_COUNT * 360));
            matrixStackIn.translate(0, 0, 1D / 16D);
            blade.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.pop();
        }
        matrixStackIn.pop();

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));

        matrixStackIn.push();
        matrixStackIn.translate(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            matrixStackIn.push();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-rotation * 2 + (float) i / BLADE_COUNT * 360));
            matrixStackIn.translate(0, 0, 1D / 16D);
            blade.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.pop();
        }
        matrixStackIn.pop();
    }

    private void renderCase(MatrixStack matrixStackIn, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        for (int i = 0; i < CASE_POINTS; i++) {
            matrixStackIn.push();
            matrixStackIn.translate(0, 0, 3F / 16F);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees((float) i / (float) CASE_POINTS * 275F - 130));
            matrixStackIn.translate(0, 0, 2.5F / 16F);
            turbineCase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 0.5f, 0.5f, 0.5f, 1.0f);
            matrixStackIn.pop();
        }
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
        for (int i = 0; i < CASE_POINTS; i++) {
            matrixStackIn.push();
            matrixStackIn.translate(0, 0, 3F / 16F);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees((float) i / (float) CASE_POINTS * 275F - 130));
            matrixStackIn.translate(0, 0, 2.5F / 16F);
            turbineCase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 0.5f, 0.5f, 0.5f, 1.0f);
            matrixStackIn.pop();
        }
        matrixStackIn.pop();

        top.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 0.4f);
    }

    private void renderPlusAndMinus(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        matrixStackIn.push();

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.LINES);

        matrixStackIn.translate(0.26D, 13.95D / 16D, 0);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
        matrixStackIn.scale(PLUS_MINUS_SCALE, PLUS_MINUS_SCALE, PLUS_MINUS_SCALE);

        // green plus
        builder.pos(-1, 0, 0).color(0, 1, 0, 1).endVertex();
        builder.pos(1, 0, 0).color(0, 1, 0, 1).endVertex();
        builder.pos(0, -1, 0).color(0, 1, 0, 1).endVertex();
        builder.pos(0, 1, 0).color(0, 1, 0, 1).endVertex();

        matrixStackIn.translate(-0.52D / PLUS_MINUS_SCALE, 0, 0);

        // red minus
        builder.pos(-1, 0, 0).color(1, 0, 0, 1).endVertex();
        builder.pos(1, 0, 0).color(1, 0, 0, 1).endVertex();

        matrixStackIn.pop();
    }
}
