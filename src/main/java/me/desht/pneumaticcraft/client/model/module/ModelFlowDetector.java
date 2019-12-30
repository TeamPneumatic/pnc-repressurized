package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.common.block.tubes.ModuleFlowDetector;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ModelFlowDetector extends ModelModuleBase<ModuleFlowDetector> {
    private static final int TUBE_PARTS = 9;

    private final RendererModel shape1;

    public ModelFlowDetector(){
        textureWidth = 64;
        textureHeight = 32;

        shape1 = new RendererModel(this, 0, 8);
        shape1.addBox(-1F, -3F, -2F, 2, 1, 5);
        shape1.setRotationPoint(0F, 16F, 4.5F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
    }

    @Override
    public void renderDynamic(ModuleFlowDetector module, float scale, float partialTicks) {
        float rot = module != null ? MathHelper.lerp(partialTicks, module.oldRotation, module.rotation) : 0f;
        for (int i = 0; i < TUBE_PARTS; i++) {
            shape1.rotateAngleZ = (float)i / TUBE_PARTS * 2 * (float)Math.PI + rot;
            shape1.render(scale);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_FLOW_DETECTOR;
    }
}
