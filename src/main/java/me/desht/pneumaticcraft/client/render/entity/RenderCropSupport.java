package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;

public class RenderCropSupport extends RenderSemiblockBase<EntityCropSupport> {
    public static final IRenderFactory<EntityCropSupport> FACTORY = RenderCropSupport::new;

    private final ModelCropSupport model = new ModelCropSupport();

    private RenderCropSupport(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EntityCropSupport p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    public void doRender(EntityCropSupport entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindEntityTexture(entity);

        AxisAlignedBB aabb = entity.getBoundingBox();
        GlStateManager.color4f(0.33f, 0.25f, 0.12f, 1F);
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y - 12 / 16F, z);
        if (entity.getTimeSinceHit() > 0) wobble(entity, partialTicks);
        GlStateManager.scaled(aabb.maxX - aabb.minX, 1, aabb.maxZ - aabb.minZ);
        model.render(1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(EntityCropSupport entity) {
        return Textures.MODEL_HEAT_FRAME;
    }
}
