package me.desht.pneumaticcraft.common.util.fakeplayer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * Drone's fake player needs a custom inventory.
 * This is so {@link EntityPlayer#getDigSpeed(IBlockState, BlockPos)} gets the right tool speed (i.e. the tool in the drone's inv. slot 0)
 * Overriding DroneFakePlayer#getItemStackFromSlot() is also necessary, but not sufficient on its own
 */
public abstract class InventoryFakePlayer extends InventoryPlayer {
    protected InventoryFakePlayer(EntityPlayer fakePlayer) {
        super(fakePlayer);
    }

    public abstract IItemHandlerModifiable getUnderlyingItemHandler();

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        return getUnderlyingItemHandler().getStackInSlot(index);
    }

    @Override
    public float getDestroySpeed(IBlockState state) {
        float f = 1.0f;

        if (!getUnderlyingItemHandler().getStackInSlot(0).isEmpty()) {
            f *= getUnderlyingItemHandler().getStackInSlot(0).getDestroySpeed(state);
        }

        return f;
    }

    @Override
    public int storeItemStack(ItemStack itemStackIn) {
        for (int i = 0; i < getUnderlyingItemHandler().getSlots(); i++) {
            if (canMerge(getStackInSlot(i), itemStackIn)) return i;
        }
        return -1;
    }

    private boolean canMerge(ItemStack stack1, ItemStack stack2) {
        return !stack1.isEmpty()
                && ItemStack.areItemStacksEqual(stack1, stack2)
                && stack1.isStackable()
                && stack1.getCount() < stack1.getMaxStackSize()
                && stack1.getCount() < this.getInventoryStackLimit();
    }

    @Override
    public int getSizeInventory() {
        return getUnderlyingItemHandler().getSlots();
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return getUnderlyingItemHandler().extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return getUnderlyingItemHandler().extractItem(index, getUnderlyingItemHandler().getSlotLimit(index), false);
    }

    @Override
    public int getFirstEmptyStack() {
        for (int i = 0; i < getUnderlyingItemHandler().getSlots(); i++) {
            if (getUnderlyingItemHandler().getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getUnderlyingItemHandler().getSlots(); i++) {
            if (!getUnderlyingItemHandler().getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        getUnderlyingItemHandler().setStackInSlot(index, stack);
    }

    @Override
    public void clear() {
        for (int i = 0; i < getUnderlyingItemHandler().getSlots(); i++) {
            getUnderlyingItemHandler().setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
