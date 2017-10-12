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
}
