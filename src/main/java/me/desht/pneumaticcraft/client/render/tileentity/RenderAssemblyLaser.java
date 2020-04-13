package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyLaser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;

public class RenderAssemblyLaser extends AbstractTileModelRenderer<TileEntityAssemblyLaser> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase1;
    private final ModelRenderer armBase2;
    private final ModelRenderer supportMiddle;
    private final ModelRenderer armMiddle1;
    private final ModelRenderer armMiddle2;
    private final ModelRenderer laserBase;
    private final ModelRenderer laser;

    private static final float TEXTURE_SIZE = 1 / 150f;

    public RenderAssemblyLaser(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 64, 0, 17);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        baseTurn.mirror = true;
        baseTurn2 = new ModelRenderer(64, 64, 28, 17);
        baseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        baseTurn2.setRotationPoint(-2F, 17F, -2F);
        baseTurn2.mirror = true;

        armBase1 = new ModelRenderer(64, 64, 0, 25);
        armBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase1.setRotationPoint(2F, 17F, -1F);
        armBase1.mirror = true;
        armBase2 = new ModelRenderer(64, 64, 0, 25);
        armBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase2.setRotationPoint(-3F, 17F, -1F);
        armBase2.mirror = true;

        supportMiddle = new ModelRenderer(64, 64, 0, 57);
        supportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        supportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        supportMiddle.mirror = true;

        armMiddle1 = new ModelRenderer(64, 64, 0, 35);
        armMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle1.setRotationPoint(-2F, 2F, 5F);
        armMiddle1.mirror = true;
        armMiddle2 = new ModelRenderer(64, 64, 0, 35);
        armMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle2.setRotationPoint(1F, 2F, 5F);
        armMiddle2.mirror = true;

        laserBase = new ModelRenderer(64, 64, 8, 38);
        laserBase.addBox(0F, 0F, 0F, 2, 2, 3);
        laserBase.setRotationPoint(-1F, 2F, 4.5F);
        laserBase.mirror = true;
        laser = new ModelRenderer(64, 64, 54, 59);
        laser.addBox(0F, 0F, 0F, 1, 1, 32);
        laser.setRotationPoint(-0.5F, 2.5F, 1F);
        laser.mirror = true;
    }

    @Override
    public void renderModel(TileEntityAssemblyLaser te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 5; i++) {
            angles[i] = MathHelper.lerp(partialTicks, te.oldAngles[i], te.angles[i]);
        }

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_ASSEMBLY_LASER_AND_DRILL));

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angles[0]));
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);
        armBase1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armBase2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        supportMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);
        armMiddle1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armMiddle2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);

        laserBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        if (te.isLaserOn) {
            matrixStackIn.push();
            matrixStackIn.translate(0, 2.75 / 16D, 1 / 16D);
            matrixStackIn.scale(1/8f, 1/8f, 1/8f);
            laser.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1f, 0.1f, 0f, 1f);
            matrixStackIn.pop();
        }

        matrixStackIn.scale(TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStackIn.translate(0, 0, 18);
        RenderUtils.drawTexture(matrixStackIn, bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GUI_LASER_DANGER)), -8, -65, combinedLightIn);
    }
}
