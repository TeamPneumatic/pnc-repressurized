package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;

public class RenderHeatFrame extends RenderSemiblockBase<EntityHeatFrame> {
    public static final IRenderFactory<EntityHeatFrame> FACTORY = RenderHeatFrame::new;

    private final ModelHeatFrame model = new ModelHeatFrame();

    private RenderHeatFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void doRender(EntityHeatFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindEntityTexture(entity);

        TintColor tint = HeatUtil.getColourForTemperature(entity.getSyncedTemperature());
        float[] f = tint.getComponents(null);
        GlStateManager.color4f(f[0], f[1], f[2], 1f);

        AxisAlignedBB aabb = entity.getBoundingBox();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.scaled(aabb.getXSize(), aabb.getYSize(), aabb.getZSize());
        GlStateManager.translated(0, -0.5, 0);
        if (entity.getTimeSinceHit() > 0) wobble(entity, partialTicks);
        model.render(1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityHeatFrame entityHeatFrame) {
        return Textures.MODEL_HEAT_FRAME;
    }
}
