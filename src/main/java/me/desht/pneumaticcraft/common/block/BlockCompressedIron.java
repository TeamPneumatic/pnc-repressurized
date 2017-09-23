package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockCompressedIron extends BlockPneumaticCraft {

    BlockCompressedIron() {
        super(Material.IRON, "compressed_iron_block");
        setSoundType(SoundType.METAL);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCompressedIronBlock.class;
    }

//    /**
//     * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
//     * when first determining what to render.
//     * TODO 1.8 test, renderpass?
//     */
//    @Override
//    @SideOnly(Side.CLIENT)
//    public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
//        TileEntityCompressedIronBlock te = (TileEntityCompressedIronBlock) world.getTileEntity(pos);
//        int heatLevel = te.getHeatLevel();
//        double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
//        return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
//    }

}
