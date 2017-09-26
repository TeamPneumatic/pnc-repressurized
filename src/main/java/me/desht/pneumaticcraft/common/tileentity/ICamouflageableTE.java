package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.state.IBlockState;

/**
 * Implement this interface in tile entities which should store a camouflaged state.  The corresponding block should
 * be a subclass of {@link me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo}
 */
public interface ICamouflageableTE {
    /**
     * Get the camouflage state; the blockstate which will be used to render this tile entity's block.  It is
     * recommended that the tile entity cache this state where possible for performance reasons.
     *
     * @return the camouflage state, or null if the block should not be camouflaged
     */
    IBlockState getCamouflage();
}
