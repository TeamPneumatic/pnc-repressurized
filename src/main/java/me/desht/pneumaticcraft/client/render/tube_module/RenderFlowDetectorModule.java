package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleFlowDetector;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderFlowDetectorModule extends TubeModuleRendererBase<ModuleFlowDetector> {
    private static final int TUBE_PARTS = 9;

    private final ModelRenderer shape1;

    public RenderFlowDetectorModule() {
        shape1 = new ModelRenderer(64, 32, 0, 8);
        shape1.addBox(-1F, -3F, -2F, 2, 1, 5);
        shape1.setRotationPoint(0F, 16F, 4.5F);
        shape1.mirror = true;
    }

    @Override
    protected void renderDynamic(ModuleFlowDetector module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        float rot = module != null ? MathHelper.lerp(partialTicks, module.oldRotation, module.rotation) : 0f;
        for (int i = 0; i < TUBE_PARTS; i++) {
            shape1.rotateAngleZ = (float)i / TUBE_PARTS * 2 * (float)Math.PI + rot;
            shape1.render(matrixStack, builder, combinedLight, combinedOverlay);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_FLOW_DETECTOR;
    }
}
