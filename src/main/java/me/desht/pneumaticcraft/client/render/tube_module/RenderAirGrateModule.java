package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderAirGrateModule extends TubeModuleRendererBase<ModuleAirGrate> {
    private final ModelRenderer top;
    private final ModelRenderer side1;
    private final ModelRenderer side2;
    private final ModelRenderer side3;
    private final ModelRenderer side4;
    private final ModelRenderer base1;
    private final ModelRenderer base2;
    private final ModelRenderer base3;

    public RenderAirGrateModule() {
        top = new ModelRenderer(128, 64, 42, 19);
        top.addBox(0F, 0F, 0F, 14F, 0F, 14F);
        top.setPos(-7F, 9F, 8F);
        top.mirror = true;
        top.xRot = -1.570796F;

        side1 = new ModelRenderer(128, 64, 0, 18);
        side1.addBox(0F, 0F, 0F, 16F, 1F, 1F);
        side1.setPos(-8F, 23F, 7F);
        side1.mirror = true;

        side2 = new ModelRenderer(128, 64, 0, 21);
        side2.addBox(0F, 0F, 0F, 16F, 1F, 1F);
        side2.setPos(-8F, 8F, 7F);
        side2.mirror = true;

        side3 = new ModelRenderer(128, 64, 50, 0);
        side3.addBox(0F, 0F, 0F, 1F, 1F, 14F);
        side3.setPos(-8F, 23F, 7F);
        side3.mirror = true;
        side3.xRot = 1.570796F;

        side4 = new ModelRenderer(128, 64, 82, 0);
        side4.addBox(0F, 0F, 0F, 1F, 1F, 14F);
        side4.setPos(7F, 23F, 7F);
        side4.mirror = true;
        side4.xRot = 1.570796F;

        base1 = new ModelRenderer(128, 64, 69, 0);
        base1.addBox(0F, 0F, 0F, 6F, 2F, 6F);
        base1.setPos(-3F, 13F, 4F);
        base1.mirror = true;
        base1.xRot = -1.570796F;

        base2 = new ModelRenderer(128, 64, 0, 25);
        base2.addBox(0F, 0F, 0F, 12F, 2F, 12F);
        base2.setPos(-6F, 10F, 6F);
        base2.mirror = true;
        base2.xRot = -1.570796F;

        base3 = new ModelRenderer(128, 64, 0, 0);
        base3.addBox(2F, 0F, 0F, 16F, 1F, 16F);
        base3.setPos(-10F, 8F, 7F);
        base3.mirror = true;
        base3.xRot = -1.570796F;
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_AIR_GRATE_UPGRADED;
        } else {
            texture = Textures.MODEL_AIR_GRATE;
        }
        return texture;
    }

    @Override
    protected void renderDynamic(ModuleAirGrate module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        top.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        side1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        side2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        side3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        side4.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        base1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        base2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        base3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }
}
