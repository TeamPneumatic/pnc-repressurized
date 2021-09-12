package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressurizedSpawner;
import net.minecraft.tileentity.TileEntity;

public class BlockPressurizedSpawner extends BlockPneumaticCraft {
    public BlockPressurizedSpawner() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressurizedSpawner.class;
    }
}
