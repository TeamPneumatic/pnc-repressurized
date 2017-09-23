package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderCoordWireframe {
    public final BlockPos pos;
    public final World world;
    public int ticksExisted;

    public RenderCoordWireframe(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public void render(float partialTicks) {
        /*
        Block block = Block.blocksList[world.getBlockId(x, y, z)];
        block.setBlockBoundsBasedOnState(world, x, y, z);
        double minX = block.getBlockBoundsMinX();
        double minY = block.getBlockBoundsMinY();
        double minZ = block.getBlockBoundsMinZ();
        double maxX = minX + (block.getBlockBoundsMaxX() - minX) * progress;
        double maxY = minY + (block.getBlockBoundsMaxY() - minY) * progress;
        double maxZ = minZ + (block.getBlockBoundsMaxX() - minZ) * progress;
        */
        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;
        float progress = (ticksExisted % 20 + partialTicks) / 20;
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(1.0F);
        // GL11.glColor4d(0, 1, 1, progress < 0.5F ? progress + 0.5F : 1.5 - progress);
        GL11.glColor4d(0, progress < 0.5F ? progress + 0.5F : 1.5 - progress, 1, 1);
        GL11.glPushMatrix();
        // GL11.glTranslated(-0.5D, -0.5D, -0.5D);
        GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(minX, minY, minZ).endVertex();
        wr.pos(minX, maxY, minZ).endVertex();
        wr.pos(minX, minY, maxZ).endVertex();
        wr.pos(minX, maxY, maxZ).endVertex();

        wr.pos(maxX, minY, minZ).endVertex();
        wr.pos(maxX, maxY, minZ).endVertex();
        wr.pos(maxX, minY, maxZ).endVertex();
        wr.pos(maxX, maxY, maxZ).endVertex();

        wr.pos(minX, minY, minZ).endVertex();
        wr.pos(maxX, minY, minZ).endVertex();
        wr.pos(minX, minY, maxZ).endVertex();
        wr.pos(maxX, minY, maxZ).endVertex();

        wr.pos(minX, maxY, minZ).endVertex();
        wr.pos(maxX, maxY, minZ).endVertex();
        wr.pos(minX, maxY, maxZ).endVertex();
        wr.pos(maxX, maxY, maxZ).endVertex();

        wr.pos(minX, minY, minZ).endVertex();
        wr.pos(minX, minY, maxZ).endVertex();
        wr.pos(maxX, minY, minZ).endVertex();
        wr.pos(maxX, minY, maxZ).endVertex();

        wr.pos(minX, maxY, minZ).endVertex();
        wr.pos(minX, maxY, maxZ).endVertex();
        wr.pos(maxX, maxY, minZ).endVertex();
        wr.pos(maxX, maxY, maxZ).endVertex();

        Tessellator.getInstance().draw();

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
