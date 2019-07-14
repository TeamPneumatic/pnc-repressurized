package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderTumblingBlock extends EntityRenderer<EntityTumblingBlock> {
    public static final IRenderFactory<EntityTumblingBlock> FACTORY = RenderTumblingBlock::new;

    private RenderTumblingBlock(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityTumblingBlock entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // mostly lifted from RenderFallingBlock
        ItemStack stack = entity.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        Block block = ((BlockItem) stack.getItem()).getBlock();
        BlockState state = block.getDefaultState();
        if (state.getRenderType() == BlockRenderType.MODEL) {
            World world = entity.getEntityWorld();
            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            if (this.renderOutlines) {
                GlStateManager.enableColorMaterial();
                GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
            }

            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            BlockPos blockpos = new BlockPos(entity.posX, entity.getBoundingBox().maxY, entity.posZ);
            // make the block tumble as it flies through the air
            GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);
            float angle = ((entity.ticksExisted + partialTicks) * 36) % 360;  // * 36 : will rotate through 360 degrees twice / second
            GlStateManager.rotated(angle, 1f, 0f, 1f);
            GlStateManager.translated(-blockpos.getX() - 1, -blockpos.getY() - 0.5, -blockpos.getZ() - 1);
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false, world.rand, state.getPositionRandom(entity.getOrigin()), null);
            tessellator.draw();

            if (this.renderOutlines) {
                GlStateManager.tearDownSolidRenderingTextureCombine();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityTumblingBlock entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
