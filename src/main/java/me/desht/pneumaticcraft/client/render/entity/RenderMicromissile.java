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
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderMicromissile extends EntityRenderer<MicromissileEntity> {
    public RenderMicromissile(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MicromissileEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        // mostly lifted from ArrowRenderer
        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90.0F));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(45.0F));

        matrixStackIn.scale(0.05625F, 0.05625F, 0.05625F);
        matrixStackIn.translate(-4.0D, 0.0D, 0.0D);

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(this.getTextureLocation(entityIn)));
        PoseStack.Pose pose = matrixStackIn.last();
        vertex(pose, builder, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLightIn);
        vertex(pose, builder, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLightIn);

        for (int j = 0; j < 4; ++j) {
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(pose, builder, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, packedLightIn);
            vertex(pose, builder, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, packedLightIn);
            vertex(pose, builder, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, packedLightIn);
            vertex(pose, builder, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, packedLightIn);
        }

        matrixStackIn.popPose();
    }

    public void vertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, float nx, float ny, float nz, int lightmap) {
        builder.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(pose, nx, nz, ny)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MicromissileEntity entity) {
        return Textures.MICROMISSILE_ENTITY;
    }
}
