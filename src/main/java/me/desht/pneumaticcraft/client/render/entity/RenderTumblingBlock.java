package me.desht.pneumaticcraft.client.render.entity;

import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderTumblingBlock extends Render<EntityTumblingBlock> {
    public static final IRenderFactory<EntityTumblingBlock> FACTORY = RenderTumblingBlock::new;

    private RenderTumblingBlock(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityTumblingBlock entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // mostly lifted from RenderFallingBlock
        ItemStack stack = entity.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
            return;
        }
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        IBlockState state = block.getStateFromMeta(stack.getMetadata());
        if (state.getRenderType() == EnumBlockRenderType.MODEL) {
            World world = entity.getEntityWorld();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            if (this.renderOutlines) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(this.getTeamColor(entity));
            }

            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
            // make the block tumble as it flies through the air
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            float angle = ((entity.ticksExisted + partialTicks) * 36) % 360;  // * 36 : will rotate through 360 degrees twice / second
            GlStateManager.rotate(angle, 1f, 0f, 1f);
            GlStateManager.translate(-blockpos.getX() - 1, -blockpos.getY() - 0.5, -blockpos.getZ() - 1);
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false, MathHelper.getPositionRandom(entity.getOrigin()));
            tessellator.draw();

            if (this.renderOutlines) {
                GlStateManager.disableOutlineMode();
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
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
