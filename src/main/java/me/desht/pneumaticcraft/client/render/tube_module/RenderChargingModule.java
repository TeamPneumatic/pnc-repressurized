package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleCharging;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderChargingModule extends TubeModuleRendererBase<ModuleCharging> {
    private final ModelRenderer tip;
    private final ModelRenderer body;
    private final ModelRenderer tubeConnector;
    private final ModelRenderer tipBottom;
    private final ModelRenderer tipTop;
    private final ModelRenderer tipRight;
    private final ModelRenderer tipLeft;

    public RenderChargingModule() {
        tip = new ModelRenderer(32, 32, 0, 11);
        tip.setRotationPoint(1.0F, 15.0F, 8.0F);
        setRotation(tip, 0.0F, 3.1416F, 0.0F);
        tip.addBox(0.0F, 0.0F, 1.5F, 2.0F, 2.0F, 1.0F);
        tip.mirror = true;

        body = new ModelRenderer(32, 32, 0, 6);
        body.setRotationPoint(1.5F, 14.5F, 6.0F);
        setRotation(body, 0.0F, 3.1416F, 0.0F);
        body.addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 2.0F);
        body.mirror = true;

        tubeConnector = new ModelRenderer(32, 32, 0, 0);
        tubeConnector.setRotationPoint(2.0F, 14.0F, 4.0F);
        setRotation(tubeConnector, 0.0F, 3.1416F, 0.0F);
        tubeConnector.addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 2.0F);
        tubeConnector.mirror = true;

        tipBottom = new ModelRenderer(32, 32, 0, 18);
        tipBottom.setRotationPoint(-1.0F, 15.0F, 7.0F);
        tipBottom.addBox(0.0F, 2.0F, -0.5F, 2.0F, 0.0F, 1.0F);

        tipTop = new ModelRenderer(32, 32, 0, 14);
        tipTop.setRotationPoint(-1.0F, 15.0F, 7.0F);
        tipTop.addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F);

        tipRight = new ModelRenderer(32, 32, 2, 15);
        tipRight.setRotationPoint(-1.0F, 16.0F, 7.0F);
        tipRight.addBox(0.0F, -1.0F, -0.5F, 0.0F, 2.0F, 1.0F);

        tipLeft = new ModelRenderer(32, 32, 0, 15);
        tipLeft.setRotationPoint(1.0F, 16.0F, 7.0F);
        tipLeft.addBox(0.0F, -1.0F, -0.5F, 0.0F, 2.0F, 1.0F);
    }

    @Override
    protected void renderDynamic(ModuleCharging module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tip.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        body.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tipBottom.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tipTop.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tipRight.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tipLeft.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_CHARGING_MODULE_UPGRADED;
        } else {
            texture = Textures.MODEL_CHARGING_MODULE;
        }
        return texture;
    }
}
