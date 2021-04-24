package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer3D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModulePressureGauge;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public class RenderPressureGaugeModule extends TubeModuleRendererBase<ModulePressureGauge> {
    private static final float GAUGE_SCALE = 0.007f;

    private final ModelRenderer tubeConnector1;
    private final ModelRenderer tubeConnector2;
    private final ModelRenderer faceplate;
    private final ModelRenderer gauge1;
    private final ModelRenderer gauge2;
    private final ModelRenderer gauge3;
    private final ModelRenderer gauge4;
    private final ModelRenderer gauge5;
    private final ModelRenderer gauge6;
    private final ModelRenderer gauge7;
    private final ModelRenderer gauge8;

    public RenderPressureGaugeModule() {
        tubeConnector1 = new ModelRenderer(64, 32, 0, 0);
        tubeConnector1.addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 3.0F);
        tubeConnector1.setPos(-1.5F, 14.5F, 2.0F);
        tubeConnector1.mirror = true;
        tubeConnector2 = new ModelRenderer(64, 32, 22, 6);
        tubeConnector2.addBox(-2.0F, -2.0F, 2.0F, 7.0F, 7.0F, 1.0F);
        tubeConnector2.setPos(-1.5F, 14.5F, 2.0F);
        tubeConnector2.mirror = true;

        faceplate = new ModelRenderer(64, 32, 0, 6);
        faceplate.addBox(-1.0F, -1.0F, 0.0F, 10.0F, 10.0F, 1.0F);
        faceplate.setPos(-4.0F, 12.0F, 5.0F);
        faceplate.mirror = true;

        gauge1 = new ModelRenderer(64, 32, 0, 17);
        gauge1.addBox(-3.0F, -2.0F, 0.0F, 1.0F, 4.0F, 1.0F);
        gauge1.setPos(-1.0F, 16.0F, 5.5F);
        gauge2 = new ModelRenderer(64, 32, 4, 17);
        gauge2.addBox(4.0F, -2.0F, 0.0F, 1.0F, 4.0F, 1.0F);
        gauge2.setPos(-1.0F, 16.0F, 5.5F);
        gauge3 = new ModelRenderer(64, 32, 8, 17);
        gauge3.addBox(3.0F, -3.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        gauge3.setPos(-1.0F, 16.0F, 5.5F);
        gauge4 = new ModelRenderer(64, 32, 12, 17);
        gauge4.addBox(3.0F, 2.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        gauge4.setPos(-1.0F, 16.0F, 5.5F);
        gauge5 = new ModelRenderer(64, 32, 8, 19);
        gauge5.addBox(-2.0F, -3.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        gauge5.setPos(-1.0F, 16.0F, 5.5F);
        gauge6 = new ModelRenderer(64, 32, 12, 19);
        gauge6.addBox(-2.0F, 2.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        gauge6.setPos(-1.0F, 16.0F, 5.5F);
        gauge7 = new ModelRenderer(64, 32, 0, 24);
        gauge7.addBox(-1.0F, 3.0F, 0.0F, 4.0F, 1.0F, 1.0F);
        gauge7.setPos(-1.0F, 16.0F, 5.5F);
        gauge8 = new ModelRenderer(64, 32, 0, 22);
        gauge8.addBox(-1.0F, -4.0F, 0.0F, 4.0F, 1.0F, 1.0F);
        gauge8.setPos(-1.0F, 16.0F, 5.5F);
    }

    @Override
    protected void renderDynamic(ModulePressureGauge module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tubeConnector1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge4.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge5.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge6.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge7.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        gauge8.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    public void renderExtras(ModulePressureGauge module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        BlockPos pos = module.getTube().getBlockPos();
        if (ClientUtils.getClientPlayer().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 256) return;

        matrixStack.pushPose();

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        matrixStack.translate(0, 1.01, 0.378);
        matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE, GAUGE_SCALE);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
        TileEntityPressureTube te = module.getTube();
        PressureGaugeRenderer3D.drawPressureGauge(matrixStack, buffer, -1, te.getCriticalPressure(), te.getDangerPressure(),
                0, te.getPressure(), 0, 0, 0xFF000000);

        matrixStack.popPose();
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_GAUGE_UPGRADED;
        } else {
            texture = Textures.MODEL_GAUGE;
        }
        return texture;
    }
}
