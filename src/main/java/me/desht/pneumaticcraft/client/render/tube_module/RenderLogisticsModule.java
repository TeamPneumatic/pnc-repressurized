package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class RenderLogisticsModule extends TubeModuleRendererBase<ModuleLogistics> {
    private final ModelRenderer base2;
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;
    private final ModelRenderer notPowered, powered, action, notEnoughAir;

    public RenderLogisticsModule() {
        notPowered = new ModelRenderer(64, 64, 24, 8);
        notPowered.addBox(0F, 0F, 0F, 6F, 2F, 6F);
        notPowered.setRotationPoint(-3F, 13F, 4F);
        notPowered.mirror = true;
        setRotation(notPowered, -1.570796F, 0F, 0F);
        powered = new ModelRenderer(64, 64, 0, 8);
        powered.addBox(0F, 0F, 0F, 6F, 2F, 6F);
        powered.setRotationPoint(-3F, 13F, 4F);
        powered.mirror = true;
        setRotation(powered, -1.570796F, 0F, 0F);
        action = new ModelRenderer(64, 64, 24, 0);
        action.addBox(0F, 0F, 0F, 6F, 2F, 6F);
        action.setRotationPoint(-3F, 13F, 4F);
        action.mirror = true;
        setRotation(action, -1.570796F, 0F, 0F);
        notEnoughAir = new ModelRenderer(64, 64, 0, 0);
        notEnoughAir.addBox(0F, 0F, 0F, 6F, 2F, 6F);
        notEnoughAir.setRotationPoint(-3F, 13F, 4F);
        notEnoughAir.mirror = true;
        setRotation(notEnoughAir, -1.570796F, 0F, 0F);

        base2 = new ModelRenderer(64, 64, 0, 25);
        base2.addBox(0F, 0F, 0F, 12F, 2F, 12F);
        base2.setRotationPoint(-6F, 10F, 6F);
        base2.mirror = true;
        setRotation(base2, -1.570796F, 0F, 0F);
        shape1 = new ModelRenderer(64, 64, 0, 39);
        shape1.addBox(0F, 0F, 0F, 1F, 13F, 1F);
        shape1.setRotationPoint(5.5F, 9.5F, 5.5F);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        shape2 = new ModelRenderer(64, 64, 4, 39);
        shape2.addBox(0F, 0F, 0F, 1F, 13F, 1F);
        shape2.setRotationPoint(-6.5F, 9.5F, 5.5F);
        shape2.mirror = true;
        setRotation(shape2, 0F, 0F, 0F);
        shape3 = new ModelRenderer(64, 64, 8, 39);
        shape3.addBox(0F, 0F, 0F, 11F, 1F, 1F);
        shape3.setRotationPoint(-5.5F, 9.5F, 5.5F);
        shape3.mirror = true;
        setRotation(shape3, 0F, 0F, 0F);
        shape4 = new ModelRenderer(64, 64, 8, 41);
        shape4.addBox(0F, 0F, 0F, 11F, 1F, 1F);
        shape4.setRotationPoint(-5.5F, 21.5F, 5.5F);
        shape4.mirror = true;
        setRotation(shape4, 0F, 0F, 0F);
    }

    @Override
    protected void renderDynamic(ModuleLogistics module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        ModelRenderer base;
        if (module.getTicksSinceAction() >= 0) {
            base = action;
        } else if (module.getTicksSinceNotEnoughAir() >= 0) {
            base = notEnoughAir;
        } else {
            base = module.hasPower() ? powered : notPowered;
        }
        base.render(matrixStack, builder, RenderUtils.FULL_BRIGHT, combinedOverlay, r, g, b, a);
        base2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);

        // the coloured frame
        float[] cols = RenderUtils.decomposeColorF(0xFF000000 | DyeColor.byId(module.getColorChannel()).getColorValue());
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        shape2.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        shape3.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        shape4.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_MODULE;
    }
}
