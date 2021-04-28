package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleFlowDetector;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderFlowDetectorModule extends TubeModuleRendererBase<ModuleFlowDetector> {
    private static final int TUBE_PARTS = 4;

    private final ModelRenderer face;

    public RenderFlowDetectorModule() {
        face = new ModelRenderer(32, 32, 0, 0);
        face.addBox(-2.0F, -3.0F, -2.0F, 4.0F, 1.0F, 5.0F);
        face.setRotationPoint(0.0F, 16.0F, 4.5F);
        face.mirror = true;
    }

    @Override
    protected void renderDynamic(ModuleFlowDetector module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        float rot = module != null ? MathHelper.lerp(partialTicks, module.oldRotation, module.rotation) : 0f;
        for (int i = 0; i < TUBE_PARTS; i++) {
            face.rotateAngleZ = (float)i / TUBE_PARTS * 2 * (float)Math.PI + rot;
            face.render(matrixStack, builder, combinedLight, combinedOverlay);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_FLOW_DETECTOR;
    }
}
