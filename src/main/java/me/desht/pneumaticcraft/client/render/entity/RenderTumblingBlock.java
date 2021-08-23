package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderTumblingBlock extends EntityRenderer<EntityTumblingBlock> {
    public static final IRenderFactory<EntityTumblingBlock> FACTORY = RenderTumblingBlock::new;

    private static final Vector3f ROT_VEC = new Vector3f(1f, 0f, 1f);

    private RenderTumblingBlock(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EntityTumblingBlock entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ItemStack stack = entity.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        Block block = ((BlockItem) stack.getItem()).getBlock();
        BlockState state = block.defaultBlockState();
        if (state.getRenderShape() == BlockRenderType.MODEL) {
            World world = entity.getCommandSenderWorld();
            if (state != world.getBlockState(entity.blockPosition()) && state.getRenderShape() != BlockRenderType.INVISIBLE) {
                matrixStackIn.pushPose();
                if (entity.tumbleVec != null) {
                    // spin the block on the x & z axes
                    matrixStackIn.translate(0, 0.5, 0);
                    float angle = ((entity.tickCount + partialTicks) * 18);
                    matrixStackIn.mulPose(entity.tumbleVec.rotationDegrees(angle));
                    matrixStackIn.translate(-0.5, -0.5, -0.5);
                }

                BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
                for (RenderType type : RenderType.chunkBufferLayers()) {
                    if (RenderTypeLookup.canRenderInLayer(state, type)) {
                        ForgeHooksClient.setRenderLayer(type);
                        blockrendererdispatcher.getModelRenderer().renderModel(world, blockrendererdispatcher.getBlockModel(state), state, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, world.getRandom(), state.getSeed(entity.getOrigin()), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                    }
                }
                ForgeHooksClient.setRenderLayer(null);
                matrixStackIn.popPose();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTumblingBlock entity) {
        return null;
    }
}
