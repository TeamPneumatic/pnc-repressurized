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
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class RenderTumblingBlock extends EntityRenderer<EntityTumblingBlock> {
    public RenderTumblingBlock(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityTumblingBlock entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
                    matrixStackIn.mulPose(entity.tumbleVec.rotationDegrees(angle));
                    matrixStackIn.translate(-0.5, -0.5, -0.5);
                }

                BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
                for (RenderType type : RenderType.chunkBufferLayers()) {
                    if (ItemBlockRenderTypes.canRenderInLayer(state, type)) {
                        ForgeHooksClient.setRenderType(type);
                        renderer.getModelRenderer().tesselateBlock(world, renderer.getBlockModel(state), state, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, world.getRandom(), state.getSeed(entity.getOrigin()), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                    }
                }
                ForgeHooksClient.setRenderType(null);
                matrixStackIn.popPose();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTumblingBlock entity) {
        return null;
    }
}
