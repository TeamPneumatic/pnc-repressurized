package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Random;

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
        BlockState state = block.getDefaultState();
        if (state.getRenderType() == BlockRenderType.MODEL) {
            World world = entity.getEntityWorld();
            if (state != world.getBlockState(new BlockPos(entity)) && state.getRenderType() != BlockRenderType.INVISIBLE) {
                matrixStackIn.push();
                BlockPos blockpos = new BlockPos(entity.getPosX(), entity.getBoundingBox().maxY, entity.getPosZ());

                // spin the block on the x & z axes
                matrixStackIn.translate(0, 0.5, 0);
                float angle = ((entity.ticksExisted + partialTicks) * 36) % 360;  // * 36 : will rotate through 360 degrees twice / second
                matrixStackIn.rotate(ROT_VEC.rotationDegrees(angle));
                matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                for (RenderType type : RenderType.getBlockRenderTypes()) {
                    if (RenderTypeLookup.canRenderInLayer(state, type)) {
                        ForgeHooksClient.setRenderLayer(type);
                        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, new Random(), state.getPositionRandom(entity.getOrigin()), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                    }
                }
                ForgeHooksClient.setRenderLayer(null);
                matrixStackIn.pop();
            }
        }
    }

    @Override
    public ResourceLocation getEntityTexture(EntityTumblingBlock entity) {
        return null;
    }
}
