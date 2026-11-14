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
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.utility.SentryTurretBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SentryTurretRenderer extends AbstractBlockEntityModelRenderer<SentryTurretBlockEntity, SentryTurretRenderer.State> {
    private final ModelMinigun model;

    public SentryTurretRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelMinigun(ctx.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SentryTurretBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.minigun.extract(blockEntity.getMinigun(), partialTicks);
    }

    @Override
    void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.translate(0, -13 / 16F, 0);
        model.submitMinigun(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.minigun, false, state.breakProgress);
    }

    public static class State extends ModelRenderState {
        final ModelMinigun.MinigunRenderState minigun = new ModelMinigun.MinigunRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(SentryTurretBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1);
    }
}
