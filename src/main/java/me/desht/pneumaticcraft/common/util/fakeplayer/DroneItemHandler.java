package me.desht.pneumaticcraft.common.util.fakeplayer;

import me.desht.pneumaticcraft.api.drone.IDrone;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class DroneItemHandler extends ItemStackHandler {
    private final IDrone holder;
    private ItemStack prevHeldStack = ItemStack.EMPTY;
    private boolean fakePlayerReady = false;

    public DroneItemHandler(IDrone holder, int size) {
        super(size);
        this.holder = holder;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack res = super.insertItem(slot, stack, simulate);
        if (res.getCount() != stack.getCount() && !simulate) {
            copyItemToFakePlayer(slot);
        }
        return res;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack res = super.extractItem(slot, amount, simulate);
        if (!res.isEmpty() && !simulate) {
            copyItemToFakePlayer(slot);
        }
        return res;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        ItemStack prevStack = getStackInSlot(slot).copy();
        super.setStackInSlot(slot, stack);
        if (!stack.isItemEqual(prevStack) || stack.getCount() != prevStack.getCount()) {
            copyItemToFakePlayer(slot);
        }
    }

    protected boolean isFakePlayerReady() {
        return fakePlayerReady;
    }

    /**
     * Call this when it's safe to create a fake player (i.e. NOT when reading NBT during entity creation!)
     */
    public void setFakePlayerReady() {
        if (!fakePlayerReady && !holder.world().isRemote) {
            fakePlayerReady = true;
            for (int slot = 0; slot < getSlots(); slot++) {
                copyItemToFakePlayer(slot);
            }
        }
    }

    /**
     * Copy item from drone's IItemHandler inventory to the fake player's main inventory
     * Also handle item equipping where appropriate
     *
     * @param slot slot that is being updated
     */
    public void copyItemToFakePlayer(int slot) {
        if (!fakePlayerReady) return;

        FakePlayer fakePlayer = holder.getFakePlayer();

        // As it stands, drone inv can't go above 36 items (max 35 inv upgrades),
        // but let's be paranoid here
        if (slot >= fakePlayer.inventory.mainInventory.size()) return;

        // not a copy: any changes to items in the fake player should also be reflected in the drone's item handler
        ItemStack newStack = getStackInSlot(slot);

        fakePlayer.inventory.mainInventory.set(slot, newStack);
        if (slot == fakePlayer.inventory.currentItem) {
            // currentItem is always 0 but we'll use that rather than a literal 0
            // maybe one day drones will be able to change their current held-item slot
            fakePlayer.setHeldItem(Hand.MAIN_HAND, newStack);
            if (!prevHeldStack.isEmpty()) {
                fakePlayer.getAttributes().removeAttributeModifiers(prevHeldStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            if (!newStack.isEmpty()) {
                fakePlayer.getAttributes().applyAttributeModifiers(newStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            prevHeldStack = newStack.copy();
        }
    }
}
