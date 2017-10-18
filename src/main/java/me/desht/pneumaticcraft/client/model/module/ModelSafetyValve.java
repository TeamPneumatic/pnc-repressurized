package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.common.block.tubes.ModuleSafetyValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelSafetyValve extends ModelModuleBase {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;

    public ModelSafetyValve(ModuleSafetyValve valve){
        textureWidth = 64;
        textureHeight = 32;

        shape1 = new ModelRenderer(this, 32, 0);
        shape1.addBox(0F, 0F, 0F, 3, 3, 2);
        shape1.setRotationPoint(-1.5F, 14.5F, 2F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        shape2 = new ModelRenderer(this, 0, 0);
        shape2.addBox(0F, 0F, 0F, 2, 2, 3);
        shape2.setRotationPoint(-1F, 15F, 4F);
        shape2.setTextureSize(64, 32);
        shape2.mirror = true;
        setRotation(shape2, 0F, 0F, 0F);
        shape3 = new ModelRenderer(this, 32, 0);
        shape3.addBox(0F, 0F, 0F, 1, 1, 3);
        shape3.setRotationPoint(2F, 15.5F, 4F);
        shape3.setTextureSize(64, 32);
        shape3.mirror = true;
        setRotation(shape3, 0F, -0.5934119F, 0F);
    }

    @Override
    protected void renderDynamic(float scale, float partialTicks) {
        shape1.render(scale);
        shape2.render(scale);
        shape3.render(scale);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_SAFETY_VALVE;
    }
}
