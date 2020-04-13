package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySpawnerAgitator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderSpawnerAgitator extends RenderSemiblockBase<EntitySpawnerAgitator> {
    public static final IRenderFactory<EntitySpawnerAgitator> FACTORY = RenderSpawnerAgitator::new;

    private static final float BRIGHTNESS = 0.2F;

    private final ModelHeatFrame model = new ModelHeatFrame();

    private RenderSpawnerAgitator(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntitySpawnerAgitator entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float g = 0.1f * MathHelper.sin((entity.world.getGameTime() + partialTicks) / 12f);

        matrixStackIn.push();
        matrixStackIn.translate(0, -0.5, 0);
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(getEntityTexture(entity)));
        model.render(matrixStackIn, builder, packedLightIn, OverlayTexture.getPackedUV(0F, false), BRIGHTNESS, 0.8f + g, BRIGHTNESS, 1f);
        matrixStackIn.pop();

    }

    @Override
    public ResourceLocation getEntityTexture(EntitySpawnerAgitator entity) {
        return Textures.MODEL_HEAT_FRAME;
    }
}
