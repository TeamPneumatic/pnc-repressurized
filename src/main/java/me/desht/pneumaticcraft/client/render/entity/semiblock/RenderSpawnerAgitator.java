package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelSpawnerAgitator;
import me.desht.pneumaticcraft.common.entity.semiblock.SpawnerAgitatorEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderSpawnerAgitator extends RenderSemiblockBase<SpawnerAgitatorEntity> {
    private static final float BRIGHTNESS = 0.2F;

    private final ModelSpawnerAgitator model;

    public RenderSpawnerAgitator(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelSpawnerAgitator(ctx.bakeLayer(PNCModelLayers.SPAWNER_AGITATOR));
    }

    @Override
    public void render(SpawnerAgitatorEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float g = 0.1f * Mth.sin((entity.level().getGameTime() + partialTicks) / 12f);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 1.5, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180F));
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.pack(0F, false), BRIGHTNESS, 0.8f + g, BRIGHTNESS, 1f);
        matrixStackIn.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(SpawnerAgitatorEntity entity) {
        return Textures.MODEL_SPAWNER_AGITATOR;
    }
}
