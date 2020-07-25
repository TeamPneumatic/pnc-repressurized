package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderEntityVortex extends EntityRenderer<EntityVortex> {
    private static final int CIRCLE_POINTS = 20;
    private static final float TEX_SCALE = 0.07F;
    private static final double RADIUS = 0.5D;

    public static final IRenderFactory<EntityVortex> FACTORY = RenderEntityVortex::new;

    private RenderEntityVortex(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityVortex entity) {
        return Textures.VORTEX_ENTITY;
    }

    @Override
    public void render(EntityVortex entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (!entity.hasRenderOffsetX()) {
            entity.setRenderOffsetX(calculateXoffset());
        }

        matrixStackIn.push();

        IVertexBuilder builder = bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(getEntityTexture(entity)));

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw)));
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));
        float incr = (float) (2 * Math.PI / CIRCLE_POINTS);
        for (float angleRads = 0f; angleRads < 2 * Math.PI; angleRads += incr) {
            matrixStackIn.push();
            matrixStackIn.translate(RADIUS * MathHelper.sin(angleRads), RADIUS * MathHelper.cos(angleRads), 0);
            renderGust(matrixStackIn, builder, entity.getRenderOffsetX(), packedLightIn);
            matrixStackIn.pop();
        }

        matrixStackIn.pop();
    }

    private float calculateXoffset() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        HandSide hs = player.getPrimaryHand();
        if (player.getHeldItemMainhand().getItem() != ModItems.VORTEX_CANNON.get()) {
            hs = hs.opposite();
        }
        // yeah, this is supposed to be asymmetric; it looks better that way
        return hs == HandSide.RIGHT ? -4.0F : 16.0F;
    }

    private void renderGust(MatrixStack matrixStackIn, IVertexBuilder wr, float xOffset, int packedLightIn) {
        float u1 = 0F;
        float u2 = 1F;
        float v1 = 0F;
        float v2 = 1F;

        matrixStackIn.scale(TEX_SCALE, TEX_SCALE, TEX_SCALE);
        matrixStackIn.translate(xOffset, 0, 0);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));

        Matrix4f posMat = matrixStackIn.getLast().getMatrix();

        wr.pos(posMat, -7.0F, -2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).tex(u1, v1).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, -2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).tex(u2, v1).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, 2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).tex(u2, v2).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, 2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).tex(u1, v2).lightmap(packedLightIn).endVertex();

        wr.pos(posMat, -7.0F, 2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).tex(u1, v1).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, 2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).tex(u2, v1).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, -2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).tex(u2, v2).lightmap(packedLightIn).endVertex();
        wr.pos(posMat, -7.0F, -2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).tex(u1, v2).lightmap(packedLightIn).endVertex();
    }
}
