package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderElevatorBase extends AbstractTileModelRenderer<TileEntityElevatorBase> {
    private static final float FACTOR = 9F / 16;

    private final ModelRenderer pole1;
    private final ModelRenderer pole2;
    private final ModelRenderer pole3;
    private final ModelRenderer pole4;
    private final ModelRenderer floor;

    public RenderElevatorBase(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        pole1 = new ModelRenderer(64, 64, 0, 17);
        pole1.addBox(0F, 0F, 0F, 2, 14, 2);
        pole1.setRotationPoint(-1F, 9F, -1F);
        pole1.mirror = true;
        pole2 = new ModelRenderer(64, 64, 0, 17);
        pole2.addBox(0F, 0F, 0F, 4, 14, 4);
        pole2.setRotationPoint(-2F, 9F, -2F);
        pole2.mirror = true;
        pole3 = new ModelRenderer(64, 64, 0, 17);
        pole3.addBox(0F, 0F, 0F, 6, 14, 6);
        pole3.setRotationPoint(-3F, 9F, -3F);
        pole3.mirror = true;
        pole4 = new ModelRenderer(64, 64, 0, 17);
        pole4.addBox(0F, 0F, 0F, 8, 14, 8);
        pole4.setRotationPoint(-4F, 9F, -4F);
        pole4.mirror = true;

        floor = new ModelRenderer(64, 64, 0, 0);
        floor.addBox(0F, 0F, 0F, 16, 1, 16);
        floor.setRotationPoint(-8F, 8F, -8F);
        floor.mirror = true;
    }

    @Override
    public void renderModel(TileEntityElevatorBase te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.extension == 0) return;

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_ELEVATOR));

        float extension = MathHelper.lerp(partialTicks, te.oldExtension, te.extension);
        renderPole(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, pole4, 0, extension);
        renderPole(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, pole3, 1, extension);
        renderPole(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, pole2, 2, extension);
        renderPole(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, pole1, 3, extension);

        floor.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }

    @Override
    protected void renderExtras(TileEntityElevatorBase te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.fakeFloorTextureUV != null && te.fakeFloorTextureUV.length == 4) {
            matrixStack.push();
            float extension = te.oldExtension + (te.extension - te.oldExtension) * partialTicks;
            matrixStack.translate(0, extension + 1.0005f, 0);
            IVertexBuilder builder = iRenderTypeBuffer.getBuffer(ModRenderTypes.getTextureRender(AtlasTexture.LOCATION_BLOCKS_TEXTURE));
            float uMin = te.fakeFloorTextureUV[0];
            float vMin = te.fakeFloorTextureUV[1];
            float uMax = te.fakeFloorTextureUV[2];
            float vMax = te.fakeFloorTextureUV[3];
            Matrix4f posMat = matrixStack.getLast().getMatrix();
            builder.pos(posMat,0, 0, 1).color(1f, 1f, 1f, 1f).tex(uMin, vMax).lightmap(combinedLightIn).endVertex();
            builder.pos(posMat,1, 0, 1).color(1f, 1f, 1f, 1f).tex(uMax, vMax).lightmap(combinedLightIn).endVertex();
            builder.pos(posMat,1, 0, 0).color(1f, 1f, 1f, 1f).tex(uMax, vMin).lightmap(combinedLightIn).endVertex();
            builder.pos(posMat,0, 0, 0).color(1f, 1f, 1f, 1f).tex(uMin, vMin).lightmap(combinedLightIn).endVertex();
            matrixStack.pop();
        }
    }

    private void renderPole(MatrixStack matrixStackIn, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn, ModelRenderer pole, int idx, float extension) {
        matrixStackIn.translate(0, -extension / 4, 0);
        matrixStackIn.push();
        matrixStackIn.translate(0, FACTOR, 0);
        matrixStackIn.scale(1, extension * 16 / 14 / 4, 1);
        matrixStackIn.translate(0, -FACTOR, 0);
        pole.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1 - idx * 0.15f, 1 - idx * 0.15f, 1 - idx * 0.15f, 1);
        matrixStackIn.pop();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityElevatorBase te) {
        return true;  // since this can get very tall
    }
}
