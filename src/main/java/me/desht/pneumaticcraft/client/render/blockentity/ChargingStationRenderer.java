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
import me.desht.pneumaticcraft.api.client.IChargingStationRenderOverride;
import me.desht.pneumaticcraft.client.ClientRegistryImpl;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ChargingStationRenderer implements BlockEntityRenderer<ChargingStationBlockEntity, ChargingStationRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public ChargingStationRenderer(BlockEntityRendererProvider.Context ctx) {
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ChargingStationBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        ItemStack renderStack = te.getChargingStackSynced();
        boolean visible = !renderStack.isEmpty() && te.nonNullLevel().isLoaded(te.getBlockPos()) && te.getCamouflage() == null;
        state.renderStack = visible ? renderStack.copy() : ItemStack.EMPTY;
        state.rotation = te.getRotation();
        state.partialTicks = partialTicks;
        itemModelResolver.updateForTopItem(state.item, state.renderStack, ItemDisplayContext.FIXED, te.getLevel(), null, (int) te.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        ItemStack renderStack = state.renderStack;
        if (renderStack.isEmpty()) return;

        poseStack.pushPose();

        IChargingStationRenderOverride override = ClientRegistryImpl.getInstance().getChargingRenderOverride(renderStack.getItem());

        if (override.onRender(poseStack, renderStack, state.partialTicks, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY)) {
            poseStack.translate(0.5, 0.5, 0.5);
            RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);
            if (!(renderStack.getItem() instanceof BlockItem)) {
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }

            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }

        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        ItemStack renderStack = ItemStack.EMPTY;
        Direction rotation = Direction.NORTH;
        float partialTicks;
        final ItemStackRenderState item = new ItemStackRenderState();
    }
}
