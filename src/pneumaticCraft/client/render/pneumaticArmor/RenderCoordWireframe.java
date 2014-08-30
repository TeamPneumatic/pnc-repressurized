package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class RenderCoordWireframe{
    public final int x, y, z;
    public final World worldObj;
    public int ticksExisted;

    public RenderCoordWireframe(World worldObj, int x, int y, int z){
        this.worldObj = worldObj;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void render(float partialTicks){
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
        GL11.glTranslated(x, y, z);
        Tessellator tess = Tessellator.instance;

        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(minX, minY, minZ);
        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(minX, maxY, maxZ);

        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(maxX, minY, maxZ);
        tess.addVertex(maxX, maxY, maxZ);

        tess.addVertex(minX, minY, minZ);
        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(maxX, minY, maxZ);

        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(minX, maxY, maxZ);
        tess.addVertex(maxX, maxY, maxZ);

        tess.addVertex(minX, minY, minZ);
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(maxX, minY, maxZ);

        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(minX, maxY, maxZ);
        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(maxX, maxY, maxZ);

        tess.draw();

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
