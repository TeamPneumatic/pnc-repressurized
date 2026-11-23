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
import me.desht.pneumaticcraft.common.block.entity.utility.TagWorkbenchBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TagWorkbenchRenderer implements BlockEntityRenderer<TagWorkbenchBlockEntity, TagWorkbenchRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public TagWorkbenchRenderer(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TagWorkbenchBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        int seed = (int) te.getBlockPos().asLong();
        state.loaded = te.nonNullLevel().isLoaded(te.getBlockPos());
        state.rotation = te.getRotation();
        state.displayed.extract(itemModelResolver, te.displayedStack, te.getLevel(), seed);
        state.paper.extract(itemModelResolver, new ItemStack(Item.byId(te.paperItemId)), te.getLevel(), seed + 1);
        state.output.extract(itemModelResolver, new ItemStack(Item.byId(te.outputItemId)), te.getLevel(), seed + 2);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.loaded) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 1, 0.5);
        DisplayTableRenderer.submitItemAt(poseStack, submitNodeCollector, state.lightCoords,
                state.displayed, 0, 0, -0.25, 0.4f, state.rotation);
        DisplayTableRenderer.submitItemAt(poseStack, submitNodeCollector, state.lightCoords,
                state.paper, -0.25, 0, 0.25, 0.4f, state.rotation);
        DisplayTableRenderer.submitItemAt(poseStack, submitNodeCollector, state.lightCoords,
                state.output, 0.25, 0, 0.25, 0.4f, state.rotation);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        boolean loaded;
        Direction rotation = Direction.NORTH;
        final DisplayTableRenderer.DisplayedItem displayed = new DisplayTableRenderer.DisplayedItem();
        final DisplayTableRenderer.DisplayedItem paper = new DisplayTableRenderer.DisplayedItem();
        final DisplayTableRenderer.DisplayedItem output = new DisplayTableRenderer.DisplayedItem();
    }
}
