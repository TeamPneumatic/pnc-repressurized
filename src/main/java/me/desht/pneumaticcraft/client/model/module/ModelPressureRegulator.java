package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelPressureRegulator extends ModelModuleBase {
    private final ModelRenderer shape1;
    private final ModelRenderer valve;
    private final ModuleRegulatorTube module;

    public ModelPressureRegulator(ModuleRegulatorTube module) {
        this.module = module;
        textureWidth = 64;
        textureHeight = 32;

        shape1 = new ModelRenderer(this, 0, 0);
        shape1.addBox(0F, 0F, 0F, 7, 7, 7);
        shape1.setRotationPoint(-3.5F, 12.5F, -3F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        valve = new ModelRenderer(this, 0, 16);
        valve.addBox(0F, 0F, 0F, 4, 4, 4);
        valve.setRotationPoint(-2F, 14F, 4F);
        valve.setTextureSize(64, 32);
        valve.mirror = true;
        setRotation(valve, 0F, 0F, 0F);
    }

    @Override
    protected void renderDynamic(float scale, float partialTicks) {
        if (module.isUpgraded()) RenderUtils.glColorHex(0xFFC0FF70);
        shape1.render(scale);
        valve.render(scale);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_REGULATOR_MODULE;
    }
}
