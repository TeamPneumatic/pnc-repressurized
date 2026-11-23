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

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ProgrammerRenderer implements BlockEntityRenderer<ProgrammerBlockEntity, ProgrammerRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public ProgrammerRenderer(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ProgrammerBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        state.loaded = te.nonNullLevel().isLoaded(te.getBlockPos());
        state.rotation = te.getRotation();
        itemModelResolver.updateForTopItem(state.item, te.displayedStack, ItemDisplayContext.FIXED, te.getLevel(), null, (int) te.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.loaded || state.item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 11/16d, 0.5);
        RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);

        // not sure why negative X/Z offsets are needed for east/west rotation, but ok...
        if (state.rotation.getAxis() == Direction.Axis.X) {
            poseStack.translate(-0.345, 0.025, -0.28);
        } else {
            poseStack.translate(0.345, 0.025, 0.28);
        }
        // lie the item flat
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(0.25f, 0.25f, 0.25f);

        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        boolean loaded;
        Direction rotation = Direction.NORTH;
        final ItemStackRenderState item = new ItemStackRenderState();
    }
}
