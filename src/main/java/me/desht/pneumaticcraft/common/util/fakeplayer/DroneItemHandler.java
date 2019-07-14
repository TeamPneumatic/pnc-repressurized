package me.desht.pneumaticcraft.common.util.fakeplayer;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;

public class DroneItemHandler extends PlayerMainInvWrapper {
    private final IDrone holder;
    protected boolean heldItemChanged = false;

    public DroneItemHandler(IDrone holder) {
        super(holder.getFakePlayer().inventory);
        this.holder = holder;
    }

    private ItemStack oldStack = ItemStack.EMPTY;

    @Override
    public int getSlots() {
        return holder.getUpgrades(EnumUpgrade.INVENTORY) + 1;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack res = super.insertItem(slot, stack, simulate);
        if (res.getCount() != stack.getCount() && !simulate && slot == 0) heldItemChanged = true;
        return res;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack res = super.extractItem(slot, amount, simulate);
        if (!res.isEmpty() && !simulate && slot == 0) heldItemChanged = true;
        return res;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot == 0) heldItemChanged = true;
        super.setStackInSlot(slot, stack);
    }

    /**
     * This should be called regularly, e.g. from the entity or tile entity update() method.  It ensures the fake
     * player has the appropriate attributes based on the held item, and can be overridden for extra functionality.
     */
    public void updateHeldItem() {
        if (heldItemChanged) {
            ItemStack newStack = getStackInSlot(0);
            if (!oldStack.isEmpty()) {
                holder.getFakePlayer().getAttributes().removeAttributeModifiers(oldStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            if (!newStack.isEmpty()) {
                holder.getFakePlayer().getAttributes().applyAttributeModifiers(newStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            oldStack = newStack.copy();

            heldItemChanged = false;
        }
    }

    public CompoundNBT serializeNBT() {
        // basically the same as ItemStackHandler serialization
        ListNBT nbtTagList = new ListNBT();
        for (int i = 0; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("Slot", i);
                getStackInSlot(i).write(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", getSlots());
        return nbt;
    }
}
