package me.desht.pneumaticcraft.client.model.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer3D;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModulePressureGauge;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ModelPressureGauge extends AbstractModelRenderer<ModulePressureGauge> {
    private static final float GAUGE_SCALE = 0.007f;

    private final ModelRenderer shape1;
    private final ModelRenderer shape2;

    public ModelPressureGauge() {
        shape1 = new ModelRenderer(64, 32, 0, 0);
        shape1.addBox(0F, 0F, 0F, 3, 3, 3);
        shape1.setRotationPoint(-1.5F, 14.5F, 2F);
        shape1.mirror = true;
        shape2 = new ModelRenderer(64, 32, 0, 6);
        shape2.addBox(0F, 0F, 0F, 8, 8, 1);
        shape2.setRotationPoint(-4F, 12F, 5F);
        shape2.mirror = true;
    }

    @Override
    protected void renderDynamic(ModulePressureGauge module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        shape2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected void renderExtras(ModulePressureGauge module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        BlockPos pos = module.getTube().getPos();
        if (ClientUtils.getClientPlayer().getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 256) return;

        float pressure = 0f;
        float dangerPressure = 5f;
        float critPressure = 7f;
        if (module != null) {
            TileEntityPressureTube base = module.getTube();
            pressure = base.getPressure();
            critPressure = base.criticalPressure;
            dangerPressure = base.dangerPressure;
        }

        matrixStack.push();

        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        matrixStack.translate(0, 1, 0.378);
        matrixStack.scale(GAUGE_SCALE, GAUGE_SCALE, GAUGE_SCALE);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(180));
        PressureGaugeRenderer3D.drawPressureGauge(matrixStack, buffer, -1, critPressure, dangerPressure, 0, pressure, 0, 0, 0xFF000000);

        matrixStack.pop();
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_GAUGE;
    }
}
