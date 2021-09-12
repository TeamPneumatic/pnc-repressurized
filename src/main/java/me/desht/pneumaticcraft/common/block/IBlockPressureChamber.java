package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;

// a marker interface
public interface IBlockPressureChamber {
    static Block.Properties pressureChamberBlockProps() {
        return ModBlocks.defaultProps().strength(3f, 20000f);
    }
}
