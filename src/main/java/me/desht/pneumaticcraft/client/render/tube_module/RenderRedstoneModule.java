package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderRedstoneModule extends TubeModuleRendererBase<ModuleRedstone> {
    private final ModelRenderer redstoneConnector;
    private final ModelRenderer faceplate;
    private final ModelRenderer tubeConnector1;
    private final ModelRenderer tubeConnector2;
    private final ModelRenderer tubeConnector3;
    private final ModelRenderer tubeConnector4;
    private final ModelRenderer tubeConnector5;
    private final ModelRenderer tubeConnector6;
    private final ModelRenderer frame1;
    private final ModelRenderer frame2;
    private final ModelRenderer frame3;
    private final ModelRenderer frame4;

    public RenderRedstoneModule() {
        this.frame1 = new ModelRenderer(64, 32, 32, 0);
        this.frame1.setRotationPoint(-4.0F, 11.5F, 6.0F);
        this.frame1.addBox(2.0F, 1.5F, -3.75F, 4.0F, 1.0F, 4.0F, 0.0F);

        this.frame2 = new ModelRenderer(64, 32, 32, 5);
        this.frame2.setRotationPoint(-4.0F, 19.5F, 6.0F);
        this.frame2.addBox(2.0F, -1.5F, -3.75F, 4.0F, 1.0F, 4.0F, 0.0F);

        this.frame3 = new ModelRenderer(64, 32, 0, 6);
        this.frame3.setRotationPoint(3.5F, 12.5F, 6.0F);
        this.frame3.addBox(-1.5F, 0.5F, -3.75F, 1.0F, 6.0F, 4.0F, 0.0F);

        this.frame4 = new ModelRenderer(64, 32, 0, 16);
        this.frame4.setRotationPoint(-4.5F, 12.5F, 6.0F);
        this.frame4.addBox(1.5F, 0.5F, -3.75F, 1.0F, 6.0F, 4.0F, 0.0F);

        this.tubeConnector1 = new ModelRenderer(64, 32, 12, 10);
        this.tubeConnector1.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector1.addBox(-2.0F, -2.0F, 1.0F, 7.0F, 7.0F, 1.0F, 0.0F);

        this.tubeConnector2 = new ModelRenderer(64, 32, 12, 18);
        this.tubeConnector2.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector2.addBox(-1.0F, -1.0F, 0.0F, 5.0F, 5.0F, 1.0F, 0.0F);

        this.tubeConnector3 = new ModelRenderer(64, 32, 28, 12);
        this.tubeConnector3.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector3.addBox(4.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F, 0.0F);

        this.tubeConnector4 = new ModelRenderer(64, 32, 28, 16);
        this.tubeConnector4.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector4.addBox(0.0F, 4.0F, 0.0F, 3.0F, 1.0F, 1.0F, 0.0F);

        this.tubeConnector5 = new ModelRenderer(64, 32, 32, 12);
        this.tubeConnector5.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector5.addBox(-2.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F, 0.0F);

        this.tubeConnector6 = new ModelRenderer(64, 32, 28, 10);
        this.tubeConnector6.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tubeConnector6.addBox(0.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, 0.0F);

        this.faceplate = new ModelRenderer(64, 32, 12, 0);
        this.faceplate.setRotationPoint(-4.0F, 12.0F, 5.0F);
        this.faceplate.addBox(0.0F, 0.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F);

        this.redstoneConnector = new ModelRenderer(64, 32, 0, 0);
        this.redstoneConnector.setRotationPoint(-1.5F, 14.5F, 6.05F);
        this.redstoneConnector.addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 3.0F, 0.0F);
    }

    @Override
    protected void renderDynamic(ModuleRedstone module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tubeConnector1.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector2.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector3.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector4.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector5.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        tubeConnector6.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);

        float[] cols = { 1f, 1f, 1f, 1f };
        if (!module.isFake()) {
            int l = module.getRedstoneDirection() == ModuleRedstone.EnumRedstoneDirection.INPUT ? module.getInputLevel() : module.getRedstoneLevel();
            cols = RenderUtils.decomposeColorF(0xFF300000 | (l * 13 << 16));
            matrixStack.push();
            matrixStack.translate(0, 0, 5.2 / 16);
            matrixStack.scale(1, 1, 0.25f + 0.72f * MathHelper.lerp(partialTicks, module.lastExtension, module.extension));
            matrixStack.translate(0, 0, -5.2 / 16);
        }
        redstoneConnector.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        if (!module.isFake()) {
            matrixStack.pop();
        }
        cols = RenderUtils.decomposeColorF(0xFF000000 | DyeColor.byId(module.getColorChannel()).getColorValue());
        frame1.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame2.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame3.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame4.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_REDSTONE_MODULE_UPGRADED;
        } else {
            texture = Textures.MODEL_REDSTONE_MODULE;
        }
        return texture;
    }
}
