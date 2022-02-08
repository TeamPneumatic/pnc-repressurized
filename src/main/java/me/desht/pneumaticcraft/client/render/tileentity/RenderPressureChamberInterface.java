package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class RenderPressureChamberInterface extends AbstractTileModelRenderer<TileEntityPressureChamberInterface> {

    private final ModelRenderer inputLeft;
    private final ModelRenderer inputRight;
    private final ModelRenderer inputBottom;
    private final ModelRenderer inputTop;
    private final ModelRenderer outputLeft;
    private final ModelRenderer outputRight;
    private final ModelRenderer outputBottom;
    private final ModelRenderer outputTop;

    public RenderPressureChamberInterface(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        inputLeft = new ModelRenderer(32, 32, 0, 0);
        inputLeft.addBox(-4.0F, -12.0F, -6.0F, 4.0F, 8.0F, 1.0F);
        inputLeft.setPos(0.0F, 24.0F, 0.0F);
        inputLeft.mirror = true;

        inputRight = new ModelRenderer(32, 32, 10, 0);
        inputRight.addBox(0.0F, -12.0F, -6.0F, 4.0F, 8.0F, 1.0F);
        inputRight.setPos(0.0F, 24.0F, 0.0F);
        inputRight.mirror = true;

        inputBottom = new ModelRenderer(32, 32, 0, 9);
        inputBottom.addBox(-4.0F, -8.0F, -5.0F, 8.0F, 4.0F, 1.0F);
        inputBottom.setPos(0.0F, 24.0F, 0.0F);
        inputBottom.mirror = false;

        inputTop = new ModelRenderer(32, 32, 0, 14);
        inputTop.addBox(-4.0F, -12.0F, -5.0F, 8.0F, 4.0F, 1.0F);
        inputTop.setPos(0.0F, 24.0F, 0.0F);
        inputTop.mirror = false;

        outputLeft = new ModelRenderer(32, 32, 0, 19);
        outputLeft.addBox(-4.0F, -12.0F, 5.0F, 4.0F, 8.0F, 1.0F);
        outputLeft.setPos(0.0F, 24.0F, 0.0F);
        outputLeft.mirror = true;

        outputRight = new ModelRenderer(32, 32, 10, 19);
        outputRight.addBox(0.0F, -12.0F, 5.0F, 4.0F, 8.0F, 1.0F);
        outputRight.setPos(0.0F, 24.0F, 0.0F);
        outputRight.mirror = true;

        outputBottom = new ModelRenderer(32, 32, 0, 9);
        outputBottom.addBox(-4.0F, -8.0F, 4.0F, 8.0F, 4.0F, 1.0F);
        outputBottom.setPos(0.0F, 24.0F, 0.0F);
        outputBottom.mirror = false;

        outputTop = new ModelRenderer(32, 32, 0, 14);
        outputTop.addBox(-4.0F, -12.0F, 4.0F, 8.0F, 4.0F, 1.0F);
        outputTop.setPos(0.0F, 24.0F, 0.0F);
        outputTop.mirror = false;
    }

    @Override
    public void renderModel(TileEntityPressureChamberInterface te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PRESSURE_CHAMBER_INTERFACE));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        float inputProgress = MathHelper.lerp(partialTicks, te.oldInputProgress, te.inputProgress) / MAX_PROGRESS;
        float outputProgress = MathHelper.lerp(partialTicks, te.oldOutputProgress, te.outputProgress) / MAX_PROGRESS;
        if (inputProgress <= 1f) {
            // REMOVED:           matrixStackIn.scale(1F - inputProgress, 1, 1);
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            inputLeft.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate((-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            inputRight.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            inputBottom.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            inputTop.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
        if (outputProgress < 1f) {
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            outputLeft.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate((-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            outputRight.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            outputBottom.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            outputTop.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
    }

    @Override
    protected void renderExtras(TileEntityPressureChamberInterface te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getStackInInterface().isEmpty()) {
            matrixStack.pushPose();

            matrixStack.translate(0.5, 0.5, 0.5);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            IBakedModel ibakedmodel = itemRenderer.getModel(te.getStackInInterface(), te.getLevel(), null);
            itemRenderer.render(te.getStackInInterface(), ItemCameraTransforms.TransformType.GROUND, true, matrixStack, buffer, combinedLightIn, combinedOverlayIn, ibakedmodel);

            matrixStack.popPose();
        }
    }
}
