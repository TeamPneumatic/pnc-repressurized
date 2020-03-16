package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityTagWorkbench;
import net.minecraft.tileentity.TileEntity;

public class BlockTagWorkbench extends BlockDisplayTable {
    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityTagWorkbench.class;
    }

}
