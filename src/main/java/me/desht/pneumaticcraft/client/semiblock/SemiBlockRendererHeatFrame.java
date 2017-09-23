package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class SemiBlockRendererHeatFrame implements ISemiBlockRenderer<SemiBlockHeatFrame> {
    private final ModelHeatFrame model = new ModelHeatFrame();

    @Override
    public void render(SemiBlockHeatFrame semiBlock, float partialTick) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_HEAT_FRAME);
        int heatLevel = semiBlock.getHeatLevel();
        double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
        GL11.glColor4d(color[0], color[1], color[2], 1);

        AxisAlignedBB aabb;
        if (semiBlock.getWorld() != null) {
//            semiBlock.getBlockState().getBlock().setBlockBoundsBasedOnState(semiBlock.getWorld(), semiBlock.getPos());
            aabb = semiBlock.getBlockState().getBlock().getSelectedBoundingBox(semiBlock.getBlockState(), semiBlock.getWorld(), semiBlock.getPos());
            BlockPos p = semiBlock.getPos();
            aabb = new AxisAlignedBB(aabb.minX - p.getX(), aabb.minY - p.getY(), aabb.minZ - p.getZ(), aabb.maxX - p.getX(), aabb.maxY - p.getY(), aabb.maxZ - p.getZ());
        } else {
            aabb = new AxisAlignedBB(1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D);
        }
        GL11.glTranslated(aabb.minX, aabb.minY, aabb.minZ);
        GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GL11.glTranslated(0.5, -0.5, 0.5);
        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GL11.glPopMatrix();
        GL11.glColor4d(1, 1, 1, 1);
    }
}
