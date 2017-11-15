package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Represents an item which can store & provide a BlockPos.  An example would be the GPS tool.
 */
public interface IPositionProvider {
    /**
     * Get a blockpos from the given ItemStack.  It is up to the implementor to decide how the blockpos should be
     * stored on the itemstack.
     *
     * @param stack the itemstack
     * @return a block position that has been retrieved from the itemstack
     */
    BlockPos getStoredPos(@Nonnull ItemStack stack);
}
