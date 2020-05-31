package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleCharging;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderChargingModule extends TubeModuleRendererBase<ModuleCharging> {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;

    public RenderChargingModule() {
        shape1 = new ModelRenderer(64, 32, 22, 0);
        shape1.addBox(0F, 0F, 0F, 2, 2, 2);
        shape1.setRotationPoint(1F, 15F, 8F);
        shape1.mirror = true;
        setRotation(shape1, 0F, 3.141593F, 0F);
        shape2 = new ModelRenderer(64, 32, 12, 0);
        shape2.addBox(0F, 0F, 0F, 3, 3, 2);
        shape2.setRotationPoint(1.5F, 14.5F, 6F);
        shape2.mirror = true;
        setRotation(shape2, 0F, 3.141593F, 0F);
        shape3 = new ModelRenderer(64, 32, 0, 0);
        shape3.addBox(0F, 0F, 0F, 4, 4, 2);
        shape3.setRotationPoint(2F, 14F, 4F);
        shape3.mirror = true;
        setRotation(shape3, 0F, 3.141593F, 0F);
    }

    @Override
    protected void renderDynamic(ModuleCharging module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        shape2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        shape3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_CHARGING_MODULE;
    }
}
