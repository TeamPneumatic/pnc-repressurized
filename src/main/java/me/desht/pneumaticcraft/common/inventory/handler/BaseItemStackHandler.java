package me.desht.pneumaticcraft.common.inventory.handler;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Item stack handler which marks its owning TE as dirty when it changes.
 * Also filters item insertion using the isItemValid() method (which returns true by default).
 */
public class BaseItemStackHandler extends ItemStackHandler {
    protected final TileEntity te;

    public BaseItemStackHandler(int size) {
        this(null, size);
    }

    public BaseItemStackHandler(TileEntity te, int size) {
        super(size);
        this.te = te;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (te != null) te.setChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
}
