package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class BlockColorHandler {
    public static void registerColorHandlers() {
        final IBlockColor heatColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                int heatLevel = 10;
                if (te instanceof TileEntityCompressedIronBlock) {
                    heatLevel = ((TileEntityCompressedIronBlock) te).getHeatLevel();
                } else if (te instanceof TileEntityVortexTube) {
                    switch (tintIndex) {
                        case 0: heatLevel = ((TileEntityVortexTube) te).getHotHeatLevel(); break;
                        case 1: heatLevel = ((TileEntityVortexTube) te).getColdHeatLevel(); break;
                    }
                }
                double[] color = TileEntityCompressedIronBlock.getColorForHeatLevel(heatLevel);
                return 0xFF000000 + ((int) (color[0] * 255) << 16) + ((int) (color[1] * 255) << 8) + (int) (color[2] * 255);
            }
            return 0xFFFFFFFF;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(heatColor,
                Blockss.COMPRESSED_IRON, Blockss.HEAT_SINK, Blockss.VORTEX_TUBE);

        final IBlockColor uvLightBoxLampColor = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                TileEntity te = blockAccess.getTileEntity(pos);
                if (te instanceof TileEntityUVLightBox) {
                    return ((TileEntityUVLightBox) te).areLightsOn ? 0xFF4000FF : 0xFFAFAFE4;
                }
            }
            return 0xFFAFAFE4;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(uvLightBoxLampColor, Blockss.UV_LIGHT_BOX);

        final IBlockColor camoColor = (state, worldIn, pos, tintIndex) -> {
            if (pos == null || worldIn == null) return 0xffffff;
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                return Minecraft.getMinecraft().getBlockColors().colorMultiplier(((ICamouflageableTE) te).getCamouflage(), te.getWorld(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        };

        for (Block b : Blockss.blocks) {
            if (b instanceof BlockPneumaticCraftCamo) {
                Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(camoColor, b);
            }
        }

        final IBlockColor aphorismTileColor = (state, worldIn, pos, tintIndex) -> {
            if (worldIn != null && pos != null) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityAphorismTile) {
                    int dmg;
                    switch (tintIndex) {
                        case 0: // border
                            dmg = ((TileEntityAphorismTile) te).getBorderColor();
                            return EnumDyeColor.byDyeDamage(dmg).getColorValue();
                        case 1: // background
                            dmg = ((TileEntityAphorismTile) te).getBackgroundColor();
                            return desaturate(EnumDyeColor.byDyeDamage(dmg).getColorValue());
                        default:
                            return 0xFFFFFF;
                    }
                }
            }
            return 0xFFFFFF;
        };
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(aphorismTileColor, Blockss.APHORISM_TILE);
    }

    private static int desaturate(int c) {
        float[] hsb = Color.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        Color color = Color.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getRGB();
    }

}
