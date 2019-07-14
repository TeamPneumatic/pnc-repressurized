package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
    BlockState getCamouflage();

    /**
     * Set the camouflage for the tile entity.  The tile entity should sync this state to the client, and force
     * any necessary re-rendering.  Storing the state as an ItemStack may be useful, since ItemStacks can be
     * marked as @DescSynced.
     *
     * @param state the camo block state
     */
    void setCamouflage(BlockState state);

    /**
     * Convenience method: get the itemstack for the given block state.
     *
     * @param state
     * @return
     */
    static ItemStack getStackForState(BlockState state) {
        return state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock().asItem());
    }

    static BlockState getStateForStack(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            Block b = ((BlockItem) stack.getItem()).getBlock();
            return b.getDefaultState();
        }
        return null;
    }

    static ItemStack readCamoStackFromNBT(CompoundNBT tag) {
        return tag.contains("camoStack") ? ItemStack.read(tag.getCompound("camoStack")) : ItemStack.EMPTY;
    }

    static void writeCamoStackToNBT(ItemStack camoStack, CompoundNBT tag) {
        if (camoStack != ItemStack.EMPTY) {
            tag.put("camoStack", camoStack.write(new CompoundNBT()));
        }
    }
}
