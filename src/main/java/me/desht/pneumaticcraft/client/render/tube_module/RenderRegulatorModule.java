package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderRegulatorModule extends TubeModuleRendererBase<ModuleRegulatorTube> {
    private final ModelRenderer tubeConnector;
    private final ModelRenderer valve1;
    private final ModelRenderer valve2;

    public RenderRegulatorModule() {
        tubeConnector = new ModelRenderer(32, 32, 0, 0);
        tubeConnector.setRotationPoint(-3.5F, 12.5F, -3.0F);
        tubeConnector.addBox(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 4.0F);
        tubeConnector.mirror = true;

        valve1 = new ModelRenderer(32, 32, 0, 11);
        valve1.setRotationPoint(-2.0F, 14.0F, 4.0F);
        valve1.addBox(-0.5F, -0.5F, -3.0F, 5.0F, 5.0F, 4.0F);
        valve1.mirror = true;

        valve2 = new ModelRenderer(32, 32, 0, 21);
        valve2.setRotationPoint(-2.0F, 14.0F, 4.0F);
        valve2.addBox(-1.0F, -1.0F, 1.0F, 6.0F, 6.0F, 2.0F);
        valve2.mirror = true;
    }

    @Override
    protected void renderDynamic(ModuleRegulatorTube module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valve1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valve2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_REGULATOR_MODULE_UPGRADED;
        } else {
            texture = Textures.MODEL_REGULATOR_MODULE;
        }
        return texture;
    }
}
