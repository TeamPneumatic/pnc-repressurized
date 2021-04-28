package me.desht.pneumaticcraft.common.util.fakeplayer;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class DroneItemHandler extends ItemStackHandler {
    private final IDrone holder;
    private int useableSlots;
    private ItemStack prevHeldStack = ItemStack.EMPTY;
    private boolean fakePlayerReady = false;

    public DroneItemHandler(IDrone holder, int useableSlots) {
        super(36);  // always has 36 slots to match a player main inv
        this.holder = holder;
        this.useableSlots = useableSlots;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot >= useableSlots) return stack;

        ItemStack res = super.insertItem(slot, stack, simulate);
        if (res.getCount() != stack.getCount() && !simulate) {
            copyItemToFakePlayer(slot);
        }
        return res;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= useableSlots) return ItemStack.EMPTY;

        ItemStack res = super.extractItem(slot, amount, simulate);
        if (!res.isEmpty() && !simulate) {
            copyItemToFakePlayer(slot);
        }
        return res;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot >= useableSlots) return;

        super.setStackInSlot(slot, stack);
        copyItemToFakePlayer(slot);
    }

    @Override
    public int getSlots() {
        return Math.min(useableSlots, super.getSlots());
    }

    public void setUseableSlots(int useableSlots) {
        this.useableSlots = useableSlots;
    }

    protected boolean isFakePlayerReady() {
        return fakePlayerReady;
    }

    /**
     * Call this when it's safe to create a fake player (i.e. NOT when reading NBT during entity/tile entity creation!)
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
     * Copy of the contents of the fake player's inventory back to this inventory (other than the item in slot 0),
     * dropping in the world any items which don't fit due to the drone's inventory being too small.  Also clears
     * the fake player inventory.
     */
    public void copyFromFakePlayer() {
        if (!fakePlayerReady) return;

        PlayerInventory fakeInv = holder.getFakePlayer().inventory;
        for (int slot = 1; slot < fakeInv.getSizeInventory(); slot++) {
            ItemStack stack = fakeInv.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                if (slot < useableSlots) {
                    // using super method here to avoid unnecessary copy of the item back to the fake player again
                    super.setStackInSlot(slot, stack);
                } else {
                    PneumaticCraftUtils.dropItemOnGround(stack, holder.world(), new BlockPos(holder.getDronePos()));
                }
                fakeInv.setInventorySlotContents(slot, ItemStack.EMPTY);
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
                fakePlayer.getAttributeManager().removeModifiers(prevHeldStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            if (!newStack.isEmpty()) {
                fakePlayer.getAttributeManager().reapplyModifiers(newStack.getAttributeModifiers(EquipmentSlotType.MAINHAND));
            }
            prevHeldStack = newStack.copy();
        }
    }
}
