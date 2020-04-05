package me.desht.pneumaticcraft.client.model.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class ModelAirGrate extends AbstractModelRenderer<ModuleAirGrate> {
    private final ModelRenderer top;
    private final ModelRenderer side1;
    private final ModelRenderer side2;
    private final ModelRenderer side3;
    private final ModelRenderer side4;
    private final ModelRenderer base1;
    private final ModelRenderer base2;
    private final ModelRenderer base3;

    public ModelAirGrate() {
        top = new ModelRenderer(32, 32, 42, 19);
        top.addBox(0F, 0F, 0F, 14, 0, 14);
        top.setRotationPoint(-7F, 9F, 8F);
        top.mirror = true;
        top.rotateAngleX = -1.570796F;
        side1 = new ModelRenderer(32, 32, 0, 18);
        side1.addBox(0F, 0F, 0F, 16, 1, 1);
        side1.setRotationPoint(-8F, 23F, 7F);
        side1.mirror = true;
        side2 = new ModelRenderer(32, 32, 0, 21);
        side2.addBox(0F, 0F, 0F, 16, 1, 1);
        side2.setRotationPoint(-8F, 8F, 7F);
        side2.mirror = true;
        side3 = new ModelRenderer(32, 32, 50, 0);
        side3.addBox(0F, 0F, 0F, 1, 1, 14);
        side3.setRotationPoint(-8F, 23F, 7F);
        side3.mirror = true;
        side3.rotateAngleX = 1.570796F;
        side4 = new ModelRenderer(32, 32, 82, 0);
        side4.addBox(0F, 0F, 0F, 1, 1, 14);
        side4.setRotationPoint(7F, 23F, 7F);
        side4.mirror = true;
        side4.rotateAngleX = 1.570796F;
        base1 = new ModelRenderer(32, 32, 69, 0);
        base1.addBox(0F, 0F, 0F, 6, 2, 6);
        base1.setRotationPoint(-3F, 13F, 4F);
        base1.mirror = true;
        base1.rotateAngleX = -1.570796F;
        base2 = new ModelRenderer(32, 32, 0, 25);
        base2.addBox(0F, 0F, 0F, 12, 2, 12);
        base2.setRotationPoint(-6F, 10F, 6F);
        base2.mirror = true;
        base2.rotateAngleX = -1.570796F;
        base3 = new ModelRenderer(32, 32, 0, 0);
        base3.addBox(2F, 0F, 0F, 16, 1, 16);
        base3.setRotationPoint(-10F, 8F, 7F);
        base3.mirror = true;
        base3.rotateAngleX = -1.570796F;
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_AIR_GRATE;
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

    @Override
    protected void renderExtras(ModuleAirGrate module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        if (module.getRangeLineRenderer().shouldRender()) {
            Direction d = module.getDirection();
            int r = -module.getGrateRange() - 1;

            matrixStack.push();

            matrixStack.translate(d.getXOffset() * r, d.getYOffset() * r, d.getZOffset() * r);
            RenderUtils.renderRangeLines(module.getRangeLineRenderer(), matrixStack, buffer.getBuffer(RenderType.LINES));

            matrixStack.pop();
        }
    }
}
