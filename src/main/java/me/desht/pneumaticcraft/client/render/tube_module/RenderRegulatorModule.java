package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderRegulatorModule extends TubeModuleRendererBase<ModuleRegulatorTube> {
    private final ModelRenderer shape1;
    private final ModelRenderer valve;

    public RenderRegulatorModule() {
        shape1 = new ModelRenderer(64, 32, 0, 0);
        shape1.addBox(0F, 0F, 0F, 7, 7, 7);
        shape1.setRotationPoint(-3.5F, 12.5F, -3F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        valve = new ModelRenderer(64, 32, 0, 16);
        valve.addBox(0F, 0F, 0F, 4, 4, 4);
        valve.setRotationPoint(-2F, 14F, 4F);
        valve.setTextureSize(64, 32);
        valve.mirror = true;
        setRotation(valve, 0F, 0F, 0F);
    }

    @Override
    protected void renderDynamic(ModuleRegulatorTube module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valve.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_REGULATOR_MODULE;
    }
}
