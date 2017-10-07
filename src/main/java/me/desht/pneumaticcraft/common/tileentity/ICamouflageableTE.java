package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

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

    /**
     * Set the camouflage item for the tile entity.
     *
     * @param stack the itemstack to use
     */
    void setCamouflage(@Nonnull ItemStack stack);

    /**
     * Get the item handler object which holds the camo item(s).  May be null if this TE doesn't store camo items
     * in an item handler.
     *
     * @return the item handler
     */
    IItemHandler getCamoInventory();
}
