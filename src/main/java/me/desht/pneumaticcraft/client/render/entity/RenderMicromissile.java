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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class RenderMicromissile extends EntityRenderer<MicromissileEntity, RenderMicromissile.State> {
    public RenderMicromissile(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MicromissileEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // mostly lifted from ArrowRenderer
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0D, 0.0D, 0.0D);

        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutCull(Textures.MICROMISSILE_ENTITY), (pose, builder) -> {
            vertex(pose, builder, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, light);
            vertex(pose, builder, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, light);
            vertex(pose, builder, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, light);
            vertex(pose, builder, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, light);
            vertex(pose, builder, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, light);
            vertex(pose, builder, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, light);
            vertex(pose, builder, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, light);
            vertex(pose, builder, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, light);

            for (int j = 0; j < 4; ++j) {
                pose.rotate(Axis.XP.rotationDegrees(90.0F));
                vertex(pose, builder, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light);
                vertex(pose, builder, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light);
                vertex(pose, builder, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light);
                vertex(pose, builder, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light);
            }
        });

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float z, float u, float v, float nx, float ny, float nz, int lightmap) {
        builder.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(pose, nx, nz, ny);
    }

    public static class State extends EntityRenderState {
        float yRot;
        float xRot;
    }
}
