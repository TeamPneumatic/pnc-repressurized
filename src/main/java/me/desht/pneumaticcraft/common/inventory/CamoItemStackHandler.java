package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.FilteredItemStackHandler;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CamoItemStackHandler extends FilteredItemStackHandler {
    private final ICamouflageableTE te;

    public CamoItemStackHandler(ICamouflageableTE te, int size) {
        super(size);
        this.te = te;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean test(Integer integer, ItemStack itemStack) {
        return itemStack.isEmpty() || itemStack.getItem() instanceof ItemBlock;
    }

    @Override
    protected void onContentsChanged(int slot) {
        te.setCamouflage(ICamouflageableTE.getStateForStack(getStackInSlot(slot)));
        if (te instanceof TileEntityPneumaticBase) {
            ((TileEntityPneumaticBase) te).rerenderTileEntity();
        }
    }
}
