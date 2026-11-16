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
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.render.tube_module.AbstractTubeModuleRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PressureTubeModuleRenderer implements BlockEntityRenderer<PressureTubeBlockEntity, PressureTubeModuleRenderer.State> {

    private final Map<Identifier, AbstractTubeModuleRenderer<?, ?>> models = new HashMap<>();
    private final BlockEntityRendererProvider.Context ctx;

    public PressureTubeModuleRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PressureTubeBlockEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);

        state.modules.clear();

        if (tile.getCamouflage() != null) {
            return;
        }

        TubeModuleItem moduleItem;
        Player player = ClientUtils.getClientPlayer();
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TubeModuleItem m) {
            moduleItem = m;
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof TubeModuleItem m) {
            moduleItem = m;
        } else {
            moduleItem = null;
        }

        List<AbstractTubeModule> modules = tile.tubeModules().toList();
        if (modules.isEmpty() && moduleItem == null)
            return;

        if (moduleItem != null && Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            // "fake" module is for showing a preview of where the module would be placed
            Direction face = player.isShiftKeyDown() ? blockHitResult.getDirection().getOpposite() : blockHitResult.getDirection();
            if (blockHitResult.getBlockPos().equals(tile.getBlockPos())
                    && player.level().getBlockEntity(blockHitResult.getBlockPos()) == tile
                    && tile.getModule(face) == null) {
                AbstractTubeModule fakeModule = moduleItem.createModule(face, tile);
                if (tile.mayPlaceModule(fakeModule)) {
                    fakeModule.markFake();
                    state.modules.add(extractModule(fakeModule, partialTicks, cameraPosition));
                }
            }
        }

        for (AbstractTubeModule m : modules) {
            state.modules.add(extractModule(m, partialTicks, cameraPosition));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (ModuleEntry<?> entry : state.modules) {
            entry.submit(poseStack, submitNodeCollector, state);
        }
    }

    private <T extends AbstractTubeModule, S extends AbstractTubeModuleRenderer.ModuleRenderState> ModuleEntry<S> extractModule(T module, float partialTicks, Vec3 cameraPosition) {
        AbstractTubeModuleRenderer<T, S> renderer = getModuleRenderer(module);
        S moduleState = renderer.createRenderState();
        renderer.extractRenderState(module, moduleState, partialTicks, cameraPosition);
        return new ModuleEntry<>(renderer, moduleState);
    }

    private <T extends AbstractTubeModule, S extends AbstractTubeModuleRenderer.ModuleRenderState> AbstractTubeModuleRenderer<T, S> getModuleRenderer(T module) {
        AbstractTubeModuleRenderer<?, ?> res = models.computeIfAbsent(module.getType(), k -> TubeModuleClientRegistry.createModel(module, ctx));
        //noinspection unchecked
        return (AbstractTubeModuleRenderer<T, S>) res;
    }

    @Override
    public AABB getRenderBoundingBox(PressureTubeBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    public static class State extends BlockEntityRenderState {
        final List<ModuleEntry<?>> modules = new ArrayList<>();
    }

    record ModuleEntry<S extends AbstractTubeModuleRenderer.ModuleRenderState>(AbstractTubeModuleRenderer<?, S> renderer, S moduleState) {
        void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, State state) {
            renderer.submit(moduleState, poseStack, submitNodeCollector, state.lightCoords, state.breakProgress);
        }
    }
}
