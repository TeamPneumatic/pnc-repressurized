package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderUniversalSensor extends AbstractTileModelRenderer<TileEntityUniversalSensor> {
    private final ModelRenderer part1;
    private final ModelRenderer part2;
    private final ModelRenderer part3;
    private final ModelRenderer part4;
    private final ModelRenderer part5;
    private final ModelRenderer part6;
    private final ModelRenderer part7;
    private final ModelRenderer part8;
    private final ModelRenderer part8_r1;
    private final ModelRenderer part9;
    private final ModelRenderer part9_r1;
    private final ModelRenderer part10;
    private final ModelRenderer part10_r1;
    private final ModelRenderer part11;
    private final ModelRenderer part11_r1;
    private final ModelRenderer part12;
    private final ModelRenderer part12_r1;
    private final ModelRenderer part13;
    private final ModelRenderer part13_r1;
    private final ModelRenderer part14;
    private final ModelRenderer part14_r1;
    private final ModelRenderer part15;
    private final ModelRenderer part15_r1;

    public RenderUniversalSensor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        part1 = new ModelRenderer(32, 32, 0, 0);
        part1.setPos(0.0F, 16.0F, 0.0F);
        part1.texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, true);

        part2 = new ModelRenderer(32, 32, 0, 9);
        part2.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part2, 0.0F, 0.0F, -0.2269F);
        part2.texOffs(0, 9).addBox(-3.0F, 4.0F, 0.5F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        part3 = new ModelRenderer(32, 32, 16, 3);
        part3.setPos(0.0F, 12.0F, 0.0F);
        setRotationAngle(part3, 0.0F, 0.0F, -0.2269F);
        part3.texOffs(16, 3).addBox(-2.0F, 1.25F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, true);

        part4 = new ModelRenderer(32, 32, 16, 0);
        part4.setPos(0.0F, 10.2F, 0.0F);
        part4.texOffs(16, 0).addBox(3.25F, 1.25F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, true);

        part5 = new ModelRenderer(32, 32, 0, 5);
        part5.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part5, 0.0F, 0.0F, -0.2269F);
        part5.texOffs(0, 5).addBox(-3.0F, 3.0F, 0.5F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        part6 = new ModelRenderer(32, 32, 0, 5);
        part6.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part6, 0.0F, 0.0F, -0.2269F);
        part6.texOffs(0, 5).addBox(-3.0F, -2.0F, 0.5F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        part7 = new ModelRenderer(32, 32, 18, 9);
        part7.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part7, 0.0F, 0.0F, -0.2269F);
        part7.texOffs(18, 9).addBox(-2.5F, -1.0F, 0.5F, 0.0F, 4.0F, 3.0F, 0.0F, true);

        part8 = new ModelRenderer(32, 32, 20, 6);
        part8.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part8, 0.0F, 0.0F, -0.2269F);


        part8_r1 = new ModelRenderer(32, 32, 20, 6);
        part8_r1.setPos(-3.0F, 3.5F, 3.5F);
        part8.addChild(part8_r1);
        setRotationAngle(part8_r1, 0.0F, 0.3927F, 0.0F);
        part8_r1.texOffs(20, 6).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        part9 = new ModelRenderer(32, 32, 20, 6);
        part9.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part9, 0.0F, 0.0F, -0.2269F);


        part9_r1 = new ModelRenderer(32, 32, 20, 6);
        part9_r1.setPos(-3.0F, -1.5F, 3.5F);
        part9.addChild(part9_r1);
        setRotationAngle(part9_r1, 0.0F, 0.3927F, 0.0F);
        part9_r1.texOffs(20, 6).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        part10 = new ModelRenderer(32, 32, 15, 2);
        part10.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part10, 0.0F, 0.0F, -0.2269F);


        part10_r1 = new ModelRenderer(32, 32, 15, 2);
        part10_r1.setPos(-2.5F, -0.5F, 3.5F);
        part10.addChild(part10_r1);
        setRotationAngle(part10_r1, 0.0F, 0.3927F, 0.0F);
        part10_r1.texOffs(15, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 4.0F, 5.0F, 0.0F, true);

        part11 = new ModelRenderer(32, 32, 8, 6);
        part11.setPos(0.0F, 9.0F, 2.0F);
        setRotationAngle(part11, 0.0F, 0.0F, -0.2269F);


        part11_r1 = new ModelRenderer(32, 32, 8, 6);
        part11_r1.setPos(-3.0F, 3.5F, -3.5F);
        part11.addChild(part11_r1);
        setRotationAngle(part11_r1, 0.0F, -0.3927F, 0.0F);
        part11_r1.texOffs(8, 6).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        part12 = new ModelRenderer(32, 32, 8, 6);
        part12.setPos(0.0F, 9.0F, 2.0F);
        setRotationAngle(part12, 0.0F, 0.0F, -0.2269F);


        part12_r1 = new ModelRenderer(32, 32, 8, 6);
        part12_r1.setPos(-3.0F, -1.5F, -3.5F);
        part12.addChild(part12_r1);
        setRotationAngle(part12_r1, 0.0F, -0.3927F, 0.0F);
        part12_r1.texOffs(8, 6).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        part13 = new ModelRenderer(32, 32, 8, 7);
        part13.setPos(0.0F, 9.0F, 2.0F);
        setRotationAngle(part13, 0.0F, 0.0F, -0.2269F);


        part13_r1 = new ModelRenderer(32, 32, 8, 7);
        part13_r1.setPos(-2.5F, -0.5F, -3.5F);
        part13.addChild(part13_r1);
        setRotationAngle(part13_r1, 0.0F, -0.3927F, 0.0F);
        part13_r1.texOffs(8, 7).addBox(0.0F, -0.5F, -5.0F, 0.0F, 4.0F, 5.0F, 0.0F, true);

        part14 = new ModelRenderer(32, 32, 28, 12);
        part14.setPos(0.0F, 9.0F, -2.0F);
        setRotationAngle(part14, 0.0F, 0.0F, -0.2269F);


        part14_r1 = new ModelRenderer(32, 32, 28, 12);
        part14_r1.setPos(-3.0F, 2.5F, 3.5F);
        part14.addChild(part14_r1);
        setRotationAngle(part14_r1, 0.0F, 0.3927F, 0.0F);
        part14_r1.texOffs(28, 12).addBox(0.0F, -3.5F, 4.0F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        part15 = new ModelRenderer(32, 32, 28, 12);
        part15.setPos(0.0F, 9.0F, 2.0F);
        setRotationAngle(part15, 0.0F, 0.0F, -0.2269F);


        part15_r1 = new ModelRenderer(32, 32, 28, 12);
        part15_r1.setPos(-3.0F, 2.5F, -3.5F);
        part15.addChild(part15_r1);
        setRotationAngle(part15_r1, 0.0F, -0.3927F, 0.0F);
        part15_r1.texOffs(28, 12).addBox(0.0F, -3.5F, -5.0F, 1.0F, 4.0F, 1.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void renderModel(TileEntityUniversalSensor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_UNIVERSAL_SENSOR));

        float rotation = MathHelper.lerp(partialTicks, te.oldDishRotation, te.dishRotation);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotation));
        part1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part6.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part7.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part8.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part9.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part10.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part11.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part12.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part13.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part14.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        part15.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
