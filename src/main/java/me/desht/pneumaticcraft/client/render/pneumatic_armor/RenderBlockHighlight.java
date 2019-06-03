package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderBlockHighlight {
    public void render(World world, BlockPos pos, float partialTicks) {
        IBlockState state = world.getBlockState(pos);

        float progress = ((world.getTotalWorldTime() & 0x1f) + partialTicks) / 32f;
        float cycle = MathHelper.sin((float) (progress * Math.PI));

        AxisAlignedBB aabb = state.getBoundingBox(world, pos);
        aabb = aabb.shrink((aabb == Block.FULL_BLOCK_AABB ? 0.05f : 0.0f) + cycle / 60);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5, -0.5, -0.5);
        GlStateManager.color(0.25f + cycle / 2f, 0.75f, 0.75f, 0.3f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        RenderUtils.renderFrame(aabb, 1/64f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
