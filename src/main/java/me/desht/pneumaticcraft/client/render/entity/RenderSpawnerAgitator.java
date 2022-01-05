package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelSpawnerAgitator;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySpawnerAgitator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderSpawnerAgitator extends RenderSemiblockBase<EntitySpawnerAgitator> {
    public static final IRenderFactory<EntitySpawnerAgitator> FACTORY = RenderSpawnerAgitator::new;

    private static final float BRIGHTNESS = 0.2F;

    private final ModelSpawnerAgitator model = new ModelSpawnerAgitator();

    private RenderSpawnerAgitator(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntitySpawnerAgitator entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float g = 0.1f * MathHelper.sin((entity.level.getGameTime() + partialTicks) / 12f);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 1.5, 0);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180F));
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.pack(0F, false), BRIGHTNESS, 0.8f + g, BRIGHTNESS, 1f);
        matrixStackIn.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpawnerAgitator entity) {
        return Textures.MODEL_SPAWNER_AGITATOR;
    }
}
