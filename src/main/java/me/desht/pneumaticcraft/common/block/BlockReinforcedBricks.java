package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.tileentity.TileEntity;

public class BlockReinforcedBricks extends BlockPneumaticCraft {
    public BlockReinforcedBricks() {
        super(ModBlocks.defaultProps().hardnessAndResistance(5.0F, 1200.0F));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }
}
