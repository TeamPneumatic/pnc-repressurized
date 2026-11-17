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
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * For block entities with an entity-style model to render
 * @param <T> the block entity type
 * @param <S> the render state type
 */
public abstract class AbstractBlockEntityModelRenderer<T extends AbstractPneumaticCraftBlockEntity, S extends AbstractBlockEntityModelRenderer.ModelRenderState> implements BlockEntityRenderer<T, S> {
    protected final ItemModelResolver itemModelResolver;

    AbstractBlockEntityModelRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        // not every block with a model renderer is rotatable (e.g. the assembly controller has no facing property)
        state.rotation = blockEntity.getBlockState().getBlock() instanceof AbstractPneumaticCraftBlock block && block.isRotatable() ?
                blockEntity.getRotation() : Direction.NORTH;
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // boilerplate translation code, common to all our model-rendering BERs, is done here

        poseStack.pushPose();

        // necessary transforms to make models render in the right place
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1f, -1f, -1f);

        // actual model rendering work
        submitModel(state, poseStack, submitNodeCollector, camera);

        poseStack.popPose();

        submitExtras(state, poseStack, submitNodeCollector, camera);
    }

    abstract void submitModel(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera);

    protected void submitExtras(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    }

    static void submitPart(SubmitNodeCollector submitNodeCollector, ModelPart part, PoseStack poseStack, RenderType renderType, ModelRenderState state) {
        submitPart(submitNodeCollector, part, poseStack, renderType, state.lightCoords, -1, state);
    }

    static void submitPart(SubmitNodeCollector submitNodeCollector, ModelPart part, PoseStack poseStack, RenderType renderType, int lightCoords, int tintColor, ModelRenderState state) {
        submitNodeCollector.submitModelPart(part, poseStack, renderType, lightCoords, OverlayTexture.NO_OVERLAY, null, tintColor, state.breakProgress);
    }

    public static class ModelRenderState extends BlockEntityRenderState {
        public Direction rotation = Direction.NORTH;
    }
}
