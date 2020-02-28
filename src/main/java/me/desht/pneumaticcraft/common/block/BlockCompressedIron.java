package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import net.minecraft.tileentity.TileEntity;

public class BlockCompressedIron extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {

    public BlockCompressedIron() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCompressedIronBlock.class;
    }
}
