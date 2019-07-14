package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderBlockHighlight {
    public void render(World world, BlockPos pos, float partialTicks) {
        BlockState state = world.getBlockState(pos);

        float progress = ((world.getGameTime() & 0x1f) + partialTicks) / 32f;
        float cycle = MathHelper.sin((float) (progress * Math.PI));
        VoxelShape shape = state.getShape(world, pos);
        float shrink = (shape == VoxelShapes.fullCube() ? 0.05f : 0f) + cycle / 60f;
        AxisAlignedBB aabb = shape.getBoundingBox().shrink(shrink);

        GlStateManager.pushMatrix();
        GlStateManager.translated(-0.5, -0.5, -0.5);
        GlStateManager.color4f(0.25f + cycle / 2f, 0.75f, 0.75f, 0.3f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();
        RenderUtils.renderFrame(aabb, 1/64f);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
