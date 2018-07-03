package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class SemiBlockRendererHeatFrame implements ISemiBlockRenderer<SemiBlockHeatFrame> {
    private final ModelHeatFrame model = new ModelHeatFrame();

    @Override
    public void render(SemiBlockHeatFrame semiBlock, float partialTick) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_HEAT_FRAME);
        int heatLevel = semiBlock.getHeatLevel();
        float[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
        GlStateManager.color(color[0], color[1], color[2], 1);

        AxisAlignedBB aabb;
        if (semiBlock.getWorld() != null) {
            aabb = semiBlock.getBlockState().getBlock().getSelectedBoundingBox(semiBlock.getBlockState(), semiBlock.getWorld(), semiBlock.getPos());
            BlockPos p = semiBlock.getPos();
            aabb = new AxisAlignedBB(aabb.minX - p.getX(), aabb.minY - p.getY(), aabb.minZ - p.getZ(), aabb.maxX - p.getX(), aabb.maxY - p.getY(), aabb.maxZ - p.getZ());
        } else {
            aabb = new AxisAlignedBB(1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D);
        }
        GlStateManager.translate(aabb.minX, aabb.minY, aabb.minZ);
        GlStateManager.scale(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GlStateManager.translate(0.5, -0.5, 0.5);
        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}
