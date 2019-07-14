package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.tileentity.TileEntity;

public class BlockCompressedIron extends BlockPneumaticCraft {

    public BlockCompressedIron() {
        super(DEFAULT_PROPS, "compressed_iron_block");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCompressedIronBlock.class;
    }
}
