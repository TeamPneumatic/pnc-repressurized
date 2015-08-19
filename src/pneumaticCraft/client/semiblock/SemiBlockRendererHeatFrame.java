package pneumaticCraft.client.semiblock;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.semiblocks.ModelHeatFrame;
import pneumaticCraft.common.semiblock.SemiBlockHeatFrame;
import pneumaticCraft.common.tileentity.TileEntityCompressedIronBlock;
import pneumaticCraft.lib.Textures;

public class SemiBlockRendererHeatFrame implements ISemiBlockRenderer<SemiBlockHeatFrame>{
    private final ModelHeatFrame model = new ModelHeatFrame();

    @Override
    public void render(SemiBlockHeatFrame semiBlock, float partialTick){
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_HEAT_FRAME);
        int heatLevel = semiBlock.getHeatLevel();
        double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
        GL11.glColor4d(color[0], color[1], color[2], 1);

        AxisAlignedBB aabb;
        if(semiBlock.getWorld() != null) {
            semiBlock.getBlock().setBlockBoundsBasedOnState(semiBlock.getWorld(), semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosY, semiBlock.getPos().chunkPosZ);
            aabb = semiBlock.getBlock().getSelectedBoundingBoxFromPool(semiBlock.getWorld(), semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosY, semiBlock.getPos().chunkPosZ);
            aabb.minX -= semiBlock.getX();
            aabb.maxX -= semiBlock.getX();
            aabb.minY -= semiBlock.getY();
            aabb.maxY -= semiBlock.getY();
            aabb.minZ -= semiBlock.getZ();
            aabb.maxZ -= semiBlock.getZ();
        } else {
            aabb = AxisAlignedBB.getBoundingBox(1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D);
        }
        GL11.glTranslated(aabb.minX, aabb.minY, aabb.minZ);
        GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GL11.glTranslated(0.5, -0.5, 0.5);
        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GL11.glPopMatrix();
        GL11.glColor4d(1, 1, 1, 1);
    }

}
