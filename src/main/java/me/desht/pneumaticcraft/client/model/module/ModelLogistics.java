package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;

public class ModelLogistics extends ModelModuleBase {
    private final ModelRenderer base2;
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;
    private final ModelRenderer notPowered, powered, action, notEnoughAir;
    private final ModuleLogistics module;

    public ModelLogistics(ModuleLogistics module) {
        this.module = module;
        textureWidth = 128;
        textureHeight = 128;

        notPowered = new ModelRenderer(this, 72, 0);
        notPowered.addBox(0F, 0F, 0F, 6, 2, 6);
        notPowered.setRotationPoint(-3F, 13F, 4F);
        notPowered.setTextureSize(128, 128);
        notPowered.mirror = true;
        setRotation(notPowered, -1.570796F, 0F, 0F);
        powered = new ModelRenderer(this, 48, 0);
        powered.addBox(0F, 0F, 0F, 6, 2, 6);
        powered.setRotationPoint(-3F, 13F, 4F);
        powered.setTextureSize(128, 128);
        powered.mirror = true;
        setRotation(powered, -1.570796F, 0F, 0F);
        action = new ModelRenderer(this, 24, 0);
        action.addBox(0F, 0F, 0F, 6, 2, 6);
        action.setRotationPoint(-3F, 13F, 4F);
        action.setTextureSize(128, 128);
        action.mirror = true;
        setRotation(action, -1.570796F, 0F, 0F);
        notEnoughAir = new ModelRenderer(this, 0, 0);
        notEnoughAir.addBox(0F, 0F, 0F, 6, 2, 6);
        notEnoughAir.setRotationPoint(-3F, 13F, 4F);
        notEnoughAir.setTextureSize(128, 128);
        notEnoughAir.mirror = true;
        setRotation(notEnoughAir, -1.570796F, 0F, 0F);

        base2 = new ModelRenderer(this, 0, 25);
        base2.addBox(0F, 0F, 0F, 12, 2, 12);
        base2.setRotationPoint(-6F, 10F, 6F);
        base2.setTextureSize(128, 128);
        base2.mirror = true;
        setRotation(base2, -1.570796F, 0F, 0F);
        shape1 = new ModelRenderer(this, 0, 39);
        shape1.addBox(0F, 0F, 0F, 1, 13, 1);
        shape1.setRotationPoint(5.5F, 9.5F, 5.5F);
        shape1.setTextureSize(128, 128);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        shape2 = new ModelRenderer(this, 4, 39);
        shape2.addBox(0F, 0F, 0F, 1, 13, 1);
        shape2.setRotationPoint(-6.5F, 9.5F, 5.5F);
        shape2.setTextureSize(128, 128);
        shape2.mirror = true;
        setRotation(shape2, 0F, 0F, 0F);
        shape3 = new ModelRenderer(this, 8, 39);
        shape3.addBox(0F, 0F, 0F, 11, 1, 1);
        shape3.setRotationPoint(-5.5F, 9.5F, 5.5F);
        shape3.setTextureSize(128, 128);
        shape3.mirror = true;
        setRotation(shape3, 0F, 0F, 0F);
        shape4 = new ModelRenderer(this, 8, 41);
        shape4.addBox(0F, 0F, 0F, 11, 1, 1);
        shape4.setRotationPoint(-5.5F, 21.5F, 5.5F);
        shape4.setTextureSize(128, 128);
        shape4.mirror = true;
        setRotation(shape4, 0F, 0F, 0F);
    }

    private void renderChannelColorFrame(float size) {
        RenderUtils.glColorHex(0xFF000000 | ItemDye.DYE_COLORS[module.getColorChannel()]);
        shape1.render(size);
        shape2.render(size);
        shape3.render(size);
        shape4.render(size);
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    protected void renderDynamic(float scale, float partialTicks) {
        ModelRenderer base;
        if (module.getTicksSinceAction() >= 0) {
            base = action;
        } else if (module.getTicksSinceNotEnoughAir() >= 0) {
            base = notEnoughAir;
        } else {
            base = module.hasPower() ? powered : notPowered;
        }
        base.render(scale);
        base2.render(scale);
        renderChannelColorFrame(scale);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_MODULE;
    }
}
