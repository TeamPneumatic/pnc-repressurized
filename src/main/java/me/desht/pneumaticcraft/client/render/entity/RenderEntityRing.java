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
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.RingEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RenderEntityRing extends EntityRenderer<RingEntity, RenderEntityRing.State> {
    public RenderEntityRing(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RingEntity ring, State state, float partialTicks) {
        super.extractRenderState(ring, state, partialTicks);

        state.visible = ring.oldRing != null;
        if (state.visible) {
            state.progress = Mth.lerp(partialTicks, ring.oldRing.getProgress(), ring.ring.getProgress());
            state.offset = new Vec3(ring.ring.endX - ring.ring.startX, ring.ring.endY - ring.ring.startY, ring.ring.endZ - ring.ring.startZ)
                    .scale(state.progress);
            state.yaw = Mth.lerp(partialTicks, ring.yRotO, ring.getYRot());
            state.pitch = Mth.lerp(partialTicks, ring.xRotO, ring.getXRot());
            state.color = ring.color;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.visible) {
            RenderUtils.submitRing(poseStack, submitNodeCollector, state.offset, state.progress, state.yaw, state.pitch, state.color);
        }
    }

    public static class State extends EntityRenderState {
        boolean visible;
        Vec3 offset = Vec3.ZERO;
        double progress;
        float yaw;
        float pitch;
        int color;
    }
}
