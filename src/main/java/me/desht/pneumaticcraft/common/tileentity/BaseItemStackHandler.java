package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Item stack handler which marks its owning TE as dirty when it changes.
 */
public class BaseItemStackHandler extends ItemStackHandler {
    protected final TileEntity te;

    public BaseItemStackHandler(int size) {
        this(null, size);
    }

    protected BaseItemStackHandler(TileEntity te, int size) {
        super(size);
        this.te = te;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (te != null) te.markDirty();
    }
}
