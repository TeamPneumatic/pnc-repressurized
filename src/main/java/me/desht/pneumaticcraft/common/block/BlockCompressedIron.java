package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.tileentity.TileEntity;

public class BlockCompressedIron extends BlockPneumaticCraft {

    public BlockCompressedIron(Properties props) {
        super(props);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCompressedIronBlock.class;
    }
}
