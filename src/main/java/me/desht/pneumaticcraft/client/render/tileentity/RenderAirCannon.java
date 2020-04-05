package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderAirCannon extends TileEntityRenderer<TileEntityAirCannon> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseFrame1;
    private final ModelRenderer baseFrame2;
    private final ModelRenderer baseFrame3;
    private final ModelRenderer baseFrame4;
    private final ModelRenderer baseFrame5;
    private final ModelRenderer baseFrame6;
    private final ModelRenderer cannon1;
    private final ModelRenderer cannon2;
    private final ModelRenderer cannon3;
    private final ModelRenderer cannon4;
    private final ModelRenderer cannon5;

    public RenderAirCannon(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 32, 36, 7);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 20F, -5F);

        baseFrame1 = new ModelRenderer(64, 32, 10, 7);
        baseFrame1.addBox(0F, 0F, 0F, 1, 5, 3);
        baseFrame1.setRotationPoint(-3.5F, 15F, -3F);
        baseFrame1.mirror = true;
        baseFrame2 = new ModelRenderer(64, 32, 10, 7);
        baseFrame2.addBox(0F, 0F, 0F, 1, 5, 3);
        baseFrame2.setRotationPoint(2.5F, 15F, -3F);
        baseFrame2.mirror = true;
        baseFrame3 = new ModelRenderer(64, 32, 18, 13);
        baseFrame3.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame3.setRotationPoint(-3.5F, 14F, -2F);
        baseFrame3.mirror = true;
        baseFrame4 = new ModelRenderer(64, 32, 18, 13);
        baseFrame4.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame4.setRotationPoint(2.5F, 14F, -2F);
        baseFrame4.mirror = true;
        baseFrame5 = new ModelRenderer(64, 32, 19, 10);
        baseFrame5.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame5.setRotationPoint(2F, 15.5F, -2F);
        baseFrame5.mirror = true;
        baseFrame6 = new ModelRenderer(64, 32, 19, 10);
        baseFrame6.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame6.setRotationPoint(-3F, 15.5F, -2F);
        baseFrame6.mirror = true;

        cannon1 = new ModelRenderer(64, 32, 24, 0);
        cannon1.addBox(0F, 3F, 0F, 2, 1, 2);
        cannon1.setRotationPoint(-1F, 15F, -2.5F);
        cannon1.mirror = true;
        cannon2 = new ModelRenderer(64, 32, 27, 3);
        cannon2.addBox(0F, 0F, 0F, 2, 8, 1);
        cannon2.setRotationPoint(-1F, 10F, -0.5F);
        cannon2.mirror = true;
        cannon3 = new ModelRenderer(64, 32, 27, 3);
        cannon3.addBox(0F, 0F, 0F, 2, 8, 1);
        cannon3.setRotationPoint(-1F, 10F, -3.5F);
        cannon3.mirror = true;
        cannon4 = new ModelRenderer(64, 32, 18, 0);
        cannon4.addBox(0F, 0F, 0F, 1, 8, 2);
        cannon4.setRotationPoint(-2F, 10F, -2.5F);
        cannon4.mirror = true;
        cannon5 = new ModelRenderer(64, 32, 18, 0);
        cannon5.addBox(0F, 0F, 0F, 1, 8, 2);
        cannon5.setRotationPoint(1F, 10F, -2.5F);
        cannon5.mirror = true;
    }

    @Override
    public void render(TileEntityAirCannon te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_AIR_CANNON));

        float angle = RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        float rotationAngle = te.rotationAngle - angle + 180F;

        matrixStackIn.translate(0.0, 0.0, -15/16D);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotationAngle));
        matrixStackIn.translate(0.0, 0.0, 15/16D);
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame6.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0.0D, 1.0D, -15/16D);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(te.heightAngle));
        matrixStackIn.translate(0.0D, -1.0D, 15/16D);
        cannon1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        cannon2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        cannon3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        cannon4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        cannon5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.pop();
    }
}
