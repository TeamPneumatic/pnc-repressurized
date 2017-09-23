package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;

public abstract class FilteredItemStackHandler extends ItemStackHandler implements BiPredicate<Integer,ItemStack> {
    public FilteredItemStackHandler(int size) {
        super(size);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (test(slot, stack))
            super.setStackInSlot(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return test(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
}
