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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberValveBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PressureChamberRenderer implements BlockEntityRenderer<PressureChamberValveBlockEntity, PressureChamberRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public PressureChamberRenderer(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PressureChamberValveBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        state.items.clear();
        if (te.multiBlockSize == 0 || !te.hasGlass || te.renderedItems.isEmpty()) return;

        state.x = te.multiBlockX - te.getBlockPos().getX() + te.multiBlockSize / 2D;
        state.y = te.multiBlockY - te.getBlockPos().getY() + 1.1; // Set to '+ 1' for normal y value.
        state.z = te.multiBlockZ - te.getBlockPos().getZ() + te.multiBlockSize / 2D;
        state.multiBlockSize = te.multiBlockSize;
        state.itemLight = ClientUtils.getLightAt(new BlockPos(te.multiBlockX + te.multiBlockSize / 2, te.multiBlockY + 1, te.multiBlockZ + te.multiBlockSize / 2));
        state.ticks = te.nonNullLevel().getGameTime() + partialTicks;

        int seed = (int) te.getBlockPos().asLong();
        for (ItemStack stack : te.renderedItems) {
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(item, stack, ItemDisplayContext.FIXED, te.getLevel(), null, seed++);
            state.items.add(item);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        List<ItemStackRenderState> items = state.items;
        if (!items.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(state.x, state.y, state.z);

            // render single item centered (looks best), multiple items arranged in a circle
            // around the centre of the chamber, radius dependent on chamber size
            float circleRadius = items.size() == 1 ? 0 : 0.33f * (state.multiBlockSize - 2);
            float degreesPerStack = 360f / items.size();

            // some gentle rotation and bobbing looks good here
            double ticks = state.ticks;
            float yBob = Mth.sin(((float) ticks  / 10) % 360) * 0.01f;
            float yRot = (float) (ticks / 2) % 360;

            for (int i = 0; i < items.size(); i++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(i * degreesPerStack));
                poseStack.translate(circleRadius, yBob + 0.2, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.scale(0.5f, 0.5f, 0.5f);

                items.get(i).submit(poseStack, submitNodeCollector, state.itemLight, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(PressureChamberValveBlockEntity blockEntity) {
        return blockEntity.getChamberAABB();
    }

    public static class State extends BlockEntityRenderState {
        double x, y, z;
        int multiBlockSize;
        int itemLight;
        double ticks;
        final List<ItemStackRenderState> items = new ArrayList<>();
    }
}
