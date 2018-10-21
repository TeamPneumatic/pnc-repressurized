package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.BaseItemStackHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * An ItemStackHandler which also supports comparator signal level calculation.
 * Smart enough to only recalculate the signal when the contents have changed.
 */
public class ComparatorItemStackHandler extends BaseItemStackHandler {
    private int signalLevel = -1;  // -1 indicates recalc needed

    public ComparatorItemStackHandler(TileEntity te, int invSize) {
        super(te, invSize);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        signalLevel = -1;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        signalLevel = -1;
    }

    /**
     * Get the comparator level for this inventory.  Follows the same rules as Container#calcRedstoneLevel()
     *
     * @return a redstone signal level based on the inventory fullness
     */
    public int getComparatorValue() {
        if (signalLevel < 0) {
            signalLevel = ItemHandlerHelper.calcRedstoneFromInventory(this);
        }
        return signalLevel;
    }

    /**
     * Force a recalculation of the comparator level.  Recalculation will be done the next time
     * getComparatorValue() is called.  May be necessary to call this if the inventory has been changed
     * indirectly, e.g. by modifying an ItemStack returned from getStackInSlot().
     */
    public void invalidateComparatorValue() {
        signalLevel = -1;
    }
}
