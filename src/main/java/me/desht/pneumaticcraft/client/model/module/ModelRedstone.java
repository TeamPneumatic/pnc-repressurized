package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;

public class ModelRedstone extends ModelModuleBase {
    private final ModelRenderer redstone_connector;
    private final ModelRenderer faceplate;
    private final ModelRenderer tube_connector;
    private final ModelRenderer frame1;
    private final ModelRenderer frame2;
    private final ModelRenderer frame3;
    private final ModelRenderer frame4;

    private final ModuleRedstone module;

    public ModelRedstone(ModuleRedstone module) {
        this.module = module;

        this.textureWidth = 64;
        this.textureHeight = 32;
        this.frame1 = new ModelRenderer(this, 39, 0);
        this.frame1.setRotationPoint(-4F, 11.5F, 6.0F);
        this.frame1.addBox(0.0F, 0.0F, 0.0F, 8, 1, 1, 0.0F);
        this.frame2 = new ModelRenderer(this, 42, 2);
        this.frame2.setRotationPoint(-4F, 19.5F, 6.0F);
        this.frame2.addBox(0.0F, 0.0F, 0.0F, 8, 1, 1, 0.0F);
        this.frame3 = new ModelRenderer(this, 59, 3);
        this.frame3.setRotationPoint(3.5F, 12.5F, 6.0F);
        this.frame3.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);
        this.frame4 = new ModelRenderer(this, 42, 4);
        this.frame4.setRotationPoint(-4.5F, 12.5F, 6.0F);
        this.frame4.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);

        this.tube_connector = new ModelRenderer(this, 30, 0);
        this.tube_connector.setRotationPoint(-1.5F, 14.5F, 2.0F);
        this.tube_connector.addBox(0.0F, 0.0F, 0.0F, 3, 3, 3, 0.0F);
        this.faceplate = new ModelRenderer(this, 12, 0);
        this.faceplate.setRotationPoint(-4.0F, 12.0F, 5.0F);
        this.faceplate.addBox(0.0F, 0.0F, 0.0F, 8, 8, 1, 0.0F);
        this.redstone_connector = new ModelRenderer(this, 0, 0);
        this.redstone_connector.setRotationPoint(-1.5F, 14.5F, 6.05F);
        this.redstone_connector.addBox(0.0F, 0.0F, 0.0F, 3, 3, 3, 0.0F);
    }

    @Override
    protected void renderDynamic(float scale, float partialTicks) {
        if (module.isUpgraded()) RenderUtils.glColorHex(0xFFA0FF60);
        this.tube_connector.render(scale);
        this.faceplate.render(scale);

        if (!module.isFake()) {
            int l = module.getRedstoneDirection() == ModuleRedstone.EnumRedstoneDirection.INPUT ? module.getInputLevel() : module.getRedstoneLevel();
            RenderUtils.glColorHex(0xFF300000 | (l * 13 << 16));
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 5.2f / 16f);
            GlStateManager.scale(1, 1, 0.25f + 0.75f * (module.lastExtension + (module.extension - module.lastExtension) * partialTicks));
            GlStateManager.translate(0, 0, -5.2f / 16f);
        }
        this.redstone_connector.render(scale);
        if (!module.isFake()) {
            GlStateManager.popMatrix();
        }

        RenderUtils.glColorHex(0xFF000000 | ItemDye.DYE_COLORS[module.getColorChannel()]);
        this.frame1.render(scale);
        this.frame2.render(scale);
        this.frame3.render(scale);
        this.frame4.render(scale);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_REDSTONE_MODULE;
    }
}
