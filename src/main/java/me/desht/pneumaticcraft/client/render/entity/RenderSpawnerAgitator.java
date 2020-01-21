package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySpawnerAgitator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;

public class RenderSpawnerAgitator extends RenderSemiblockBase<EntitySpawnerAgitator> {
    public static final IRenderFactory<EntitySpawnerAgitator> FACTORY = RenderSpawnerAgitator::new;

    private static final float BRIGHTNESS = 0.2F;

    private final ModelHeatFrame model = new ModelHeatFrame();

    private RenderSpawnerAgitator(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void doRender(EntitySpawnerAgitator entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindEntityTexture(entity);

        float g = 0.1f * MathHelper.sin((entity.world.getGameTime() + partialTicks) / 12f);
        GlStateManager.color4f(BRIGHTNESS, 0.8f + g, BRIGHTNESS, 1);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y - 0.5, z);
        if (entity.getTimeSinceHit() > 0) wobble(entity, partialTicks);
        model.render(1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySpawnerAgitator entity) {
        return Textures.MODEL_HEAT_FRAME;
    }
}
