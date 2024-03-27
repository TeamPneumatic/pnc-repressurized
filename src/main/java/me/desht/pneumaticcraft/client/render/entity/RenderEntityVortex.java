/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.entity.projectile.VortexEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;

public class RenderEntityVortex extends EntityRenderer<VortexEntity> {
    private static final int CIRCLE_POINTS = 20;
    private static final float TEX_SCALE = 0.07F;
    private static final double RADIUS = 0.5D;

    public RenderEntityVortex(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(VortexEntity entity) {
        return Textures.VORTEX_ENTITY;
    }

    @Override
    public void render(VortexEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (!entity.hasRenderOffsetX()) {
            entity.setRenderOffsetX(calculateXoffset());
        }

        matrixStackIn.pushPose();

        VertexConsumer builder = bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(getTextureLocation(entity)));

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        float incr = (float) (2 * Math.PI / CIRCLE_POINTS);
        for (float angleRads = 0f; angleRads < 2 * Math.PI; angleRads += incr) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(RADIUS * Mth.sin(angleRads), RADIUS * Mth.cos(angleRads), 0);
            renderGust(matrixStackIn, builder, entity.getRenderOffsetX(), packedLightIn);
            matrixStackIn.popPose();
        }

        matrixStackIn.popPose();
    }

    private float calculateXoffset() {
        LocalPlayer player = Minecraft.getInstance().player;
        HumanoidArm hs = player.getMainArm();
        if (player.getMainHandItem().getItem() != ModItems.VORTEX_CANNON.get()) {
            hs = hs.getOpposite();
        }
        // yeah, this is supposed to be asymmetric; it looks better that way
        return hs == HumanoidArm.RIGHT ? -4.0F : 16.0F;
    }

    private void renderGust(PoseStack matrixStackIn, VertexConsumer wr, float xOffset, int packedLightIn) {
        float u1 = 0F;
        float u2 = 1F;
        float v1 = 0F;
        float v2 = 1F;

        matrixStackIn.scale(TEX_SCALE, TEX_SCALE, TEX_SCALE);
        matrixStackIn.translate(xOffset, 0, 0);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(90));

        Matrix4f posMat = matrixStackIn.last().pose();

        wr.vertex(posMat, -7.0F, -2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).uv(u1, v1).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, -2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).uv(u2, v1).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, 2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).uv(u2, v2).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, 2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).uv(u1, v2).uv2(packedLightIn).endVertex();

        wr.vertex(posMat, -7.0F, 2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).uv(u1, v1).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, 2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).uv(u2, v1).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, -2.0F, 2.0F).color(1f, 1f, 1f, 0.5f).uv(u2, v2).uv2(packedLightIn).endVertex();
        wr.vertex(posMat, -7.0F, -2.0F, -2.0F).color(1f, 1f, 1f, 0.5f).uv(u1, v2).uv2(packedLightIn).endVertex();
    }
}
