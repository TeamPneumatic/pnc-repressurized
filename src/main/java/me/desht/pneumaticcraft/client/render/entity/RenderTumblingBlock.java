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
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class RenderTumblingBlock extends EntityRenderer<TumblingBlockEntity, RenderTumblingBlock.State> {
    public RenderTumblingBlock(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public boolean shouldRender(TumblingBlockEntity entity, Frustum culler, double camX, double camY, double camZ) {
        return super.shouldRender(entity, culler, camX, camY, camZ)
                && getBlockState(entity.getStack()).getRenderShape() == RenderShape.MODEL
                && getBlockState(entity.getStack()) != entity.level().getBlockState(entity.blockPosition());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TumblingBlockEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        state.movingBlockRenderState.randomSeedPos = entity.getOrigin();
        state.movingBlockRenderState.blockPos = pos;
        state.movingBlockRenderState.blockState = getBlockState(entity.getStack());
        if (entity.level() instanceof ClientLevel clientLevel) {
            state.movingBlockRenderState.biome = clientLevel.getBiome(pos);
            state.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
            state.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
        }
        state.tumbleVec = entity.tumbleVec;
        state.angle = (entity.tickCount + partialTicks) * 18;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.movingBlockRenderState.blockState.getRenderShape() == RenderShape.MODEL) {
            poseStack.pushPose();
            if (state.tumbleVec != null) {
                // spin the block on the x & z axes
                poseStack.translate(0, 0.5, 0);
                poseStack.mulPose(Axis.of(state.tumbleVec).rotationDegrees(state.angle));
                poseStack.translate(-0.5, -0.5, -0.5);
            }
            submitNodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState);
            poseStack.popPose();

            super.submit(state, poseStack, submitNodeCollector, camera);
        }
    }

    private static BlockState getBlockState(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    public static class State extends EntityRenderState {
        final MovingBlockRenderState movingBlockRenderState = new MovingBlockRenderState();
        @Nullable Vector3f tumbleVec;
        float angle;
    }
}
