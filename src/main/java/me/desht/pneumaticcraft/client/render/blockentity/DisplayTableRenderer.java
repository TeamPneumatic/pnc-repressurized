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
import me.desht.pneumaticcraft.common.block.DisplayTableBlock;
import me.desht.pneumaticcraft.common.block.entity.utility.DisplayTableBlockEntity;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DisplayTableRenderer implements BlockEntityRenderer<DisplayTableBlockEntity, DisplayTableRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public DisplayTableRenderer(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DisplayTableBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        state.loaded = te.nonNullLevel().isLoaded(te.getBlockPos());
        state.rotation = te.getRotation();
        state.yOff = te.getBlockState().getBlock() instanceof DisplayTableBlock.Shelf shelf ? 1d - shelf.getTableHeight() : 0d;
        state.item.extract(itemModelResolver, te.displayedStack, te.getLevel(), (int) te.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.loaded) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 1, 0.5);
        submitItemAt(poseStack, submitNodeCollector, state.lightCoords, state.item, 0d, state.yOff, 0d, 0.5f, state.rotation);
        poseStack.popPose();
    }

    static void submitItemAt(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DisplayedItem item, double xOffset, double yOffset, double zOffset, float scale, Direction rot) {
        if (!item.render.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, -yOffset, 0);
            // kludge for Y-axis rotation: just using rot.getYRot() here seems to give reversed orientation on east/west axis?
            poseStack.mulPose(Axis.YP.rotationDegrees(rot.getAxis() == Direction.Axis.X ? rot.getOpposite().toYRot() : rot.toYRot()));
            if (item.blockItem) {
                poseStack.translate(xOffset, scale / 4d, zOffset);
            } else {
                // lie items flat
                poseStack.translate(xOffset, 0.025, zOffset);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            poseStack.scale(scale, scale, scale);
            item.render.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    /**
     * An item displayed on a table-like block
     */
    static class DisplayedItem {
        final ItemStackRenderState render = new ItemStackRenderState();
        boolean blockItem;

        void extract(ItemModelResolver itemModelResolver, ItemStack stack, @Nullable Level level, int seed) {
            itemModelResolver.updateForTopItem(render, stack, ItemDisplayContext.FIXED, level, null, seed);
            blockItem = stack.getItem() instanceof BlockItem;
        }
    }

    public static class State extends BlockEntityRenderState {
        boolean loaded;
        Direction rotation = Direction.NORTH;
        double yOff;
        final DisplayedItem item = new DisplayedItem();
    }
}
