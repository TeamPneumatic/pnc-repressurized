package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleSafetyValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSafetyValveModule extends TubeModuleRendererBase<ModuleSafetyValve> {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;

    public RenderSafetyValveModule(){
        shape1 = new ModelRenderer(64, 32, 32, 0);
        shape1.addBox(0F, 0F, 0F, 3, 3, 2);
        shape1.setPos(-1.5F, 14.5F, 2F);
        shape1.mirror = true;
        shape2 = new ModelRenderer(64, 32, 0, 0);
        shape2.addBox(0F, 0F, 0F, 2, 2, 3);
        shape2.setPos(-1F, 15F, 4F);
        shape2.mirror = true;
        shape3 = new ModelRenderer(64, 32, 32, 0);
        shape3.addBox(0F, 0F, 0F, 1, 1, 3);
        shape3.setPos(2F, 15.5F, 4F);
        shape3.mirror = true;
        setRotation(shape3, 0F, -0.5934119F, 0F);
    }

    @Override
    protected void renderDynamic(ModuleSafetyValve module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        shape2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        shape3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_SAFETY_VALVE;
    }
}
