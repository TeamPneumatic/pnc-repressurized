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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class RenderTumblingBlock extends EntityRenderer<TumblingBlockEntity> {
    public RenderTumblingBlock(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(TumblingBlockEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        ItemStack stack = entity.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        Block block = ((BlockItem) stack.getItem()).getBlock();
        BlockState state = block.defaultBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level world = entity.getCommandSenderWorld();
            if (state != world.getBlockState(entity.blockPosition()) && state.getRenderShape() != RenderShape.INVISIBLE) {
                matrixStackIn.pushPose();
                if (entity.tumbleVec != null) {
                    // spin the block on the x & z axes
                    matrixStackIn.translate(0, 0.5, 0);
                    float angle = ((entity.tickCount + partialTicks) * 18);
                    matrixStackIn.mulPose(Axis.of(entity.tumbleVec).rotationDegrees(angle));
                    matrixStackIn.translate(-0.5, -0.5, -0.5);
                }

                BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
                BakedModel blockModel = renderer.getBlockModel(state);
                for (RenderType type : blockModel.getRenderTypes(state, world.getRandom(), ModelData.EMPTY)) {
                    renderer.getModelRenderer().tesselateBlock(world, blockModel, state, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, world.getRandom(), state.getSeed(entity.getOrigin()), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
                }
                matrixStackIn.popPose();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TumblingBlockEntity entity) {
        return null;
    }
}
