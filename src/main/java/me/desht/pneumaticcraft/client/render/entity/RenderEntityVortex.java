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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;

public class RenderEntityVortex extends EntityRenderer<VortexEntity, RenderEntityVortex.State> {
    private static final int CIRCLE_POINTS = 20;
    private static final float TEX_SCALE = 0.07F;
    private static final double RADIUS = 0.5D;

    public RenderEntityVortex(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(VortexEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        if (!entity.hasRenderOffsetX()) {
            entity.setRenderOffsetX(calculateXoffset());
        }
        state.renderOffsetX = entity.getRenderOffsetX();
        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        float incr = (float) (2 * Math.PI / CIRCLE_POINTS);
        for (float angleRads = 0f; angleRads < 2 * Math.PI; angleRads += incr) {
            poseStack.pushPose();
            poseStack.translate(RADIUS * Mth.sin(angleRads), RADIUS * Mth.cos(angleRads), 0);
            poseStack.scale(TEX_SCALE, TEX_SCALE, TEX_SCALE);
            poseStack.translate(state.renderOffsetX, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.getTextureRenderColored(Textures.VORTEX_ENTITY),
                    (pose, builder) -> renderGust(pose.pose(), builder, state.lightCoords));
            poseStack.popPose();
        }

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
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

    private static void renderGust(Matrix4f posMat, VertexConsumer wr, int packedLightIn) {
        float u1 = 0F;
        float u2 = 1F;
        float v1 = 0F;
        float v2 = 1F;
        wr.addVertex(posMat, -7.0F, -2.0F, -2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u1, v1).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, -2.0F, 2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u2, v1).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, 2.0F, 2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u2, v2).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, 2.0F, -2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u1, v2).setLight(packedLightIn);

        wr.addVertex(posMat, -7.0F, 2.0F, -2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u1, v1).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, 2.0F, 2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u2, v1).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, -2.0F, 2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u2, v2).setLight(packedLightIn);
        wr.addVertex(posMat, -7.0F, -2.0F, -2.0F).setColor(1f, 1f, 1f, 0.5f).setUv(u1, v2).setLight(packedLightIn);
    }

    public static class State extends EntityRenderState {
        float renderOffsetX;
        float yRot;
        float xRot;
    }
}
