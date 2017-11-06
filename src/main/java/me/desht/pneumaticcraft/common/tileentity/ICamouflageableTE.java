package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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
     * Set the camouflage for the tile entity.  The tile entity should sync this state to the client, and force
     * any necessary re-rendering.  Storing the state as an ItemStack may be useful, since ItemStacks can be
     * marked as @DescSynced.
     *
     * @param state the camo block state
     */
    void setCamouflage(IBlockState state);

    /**
     * Convenience method: get the itemstack for the given block state.
     *
     * @param state
     * @return
     */
    static ItemStack getStackForState(IBlockState state) {
        return state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
    }

    static IBlockState getStateForStack(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock) {
            Block b = ((ItemBlock) stack.getItem()).getBlock();
            return b.getStateFromMeta(stack.getMetadata());
        }
        return null;
    }

    static ItemStack readCamoStackFromNBT(NBTTagCompound tag) {
        return tag.hasKey("camoStack") ? new ItemStack(tag.getCompoundTag("camoStack")) : ItemStack.EMPTY;
    }

    static void writeCamoStackToNBT(ItemStack camoStack, NBTTagCompound tag) {
        if (camoStack != ItemStack.EMPTY) {
            NBTTagCompound camoTag = new NBTTagCompound();
            camoStack.writeToNBT(camoTag);
            tag.setTag("camoStack", camoTag);
        }
    }
}
