package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockColorHandler {
    public static void registerColorHandlers() {
        final IBlockColor compressedIronHeatColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityCompressedIronBlock) {
                    int heatLevel = ((TileEntityCompressedIronBlock) te).getHeatLevel();
                    double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
                    System.out.println("get block color @ " + pos + " = " + (0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255)));
                    return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
                }
            }
            return 0xFFFFFFFF;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(compressedIronHeatColor, Blockss.COMPRESSED_IRON, Blockss.HEAT_SINK);

//        final IBlockColor etchingAcidColor = (state, worldIn, pos, tintIndex) -> 0x501c00;
//        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(etchingAcidColor, Fluids.ETCHING_ACID.getBlock());
    }

}
