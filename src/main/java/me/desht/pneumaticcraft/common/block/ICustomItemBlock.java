package me.desht.pneumaticcraft.common.block;

import net.minecraft.item.BlockItem;

/**
 * Use for blocks which need to provide a custom block item instead of the default "new BlockItem(block)".
 */
public interface ICustomItemBlock {
    /**
     * Return a custom block item here, or null if the block should not have a block item at all.
     * @return the block item, or null for no block item
     */
    BlockItem getCustomItemBlock();
}
