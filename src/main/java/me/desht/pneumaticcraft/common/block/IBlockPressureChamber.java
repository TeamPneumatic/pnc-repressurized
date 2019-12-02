package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.Block;

// a marker interface
public interface IBlockPressureChamber {
    static Block.Properties getPressureChamberBlockProps() {
        return BlockPneumaticCraft.getDefaultProps().hardnessAndResistance(3f, 2000f);
    }
}
