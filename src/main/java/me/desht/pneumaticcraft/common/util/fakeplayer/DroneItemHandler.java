/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util.fakeplayer;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.ItemStackHandler;

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
     * Call this when it's safe to create a fake player (i.e. NOT when reading NBT during entity/block entity creation!)
     */
    public void setFakePlayerReady() {
        if (!fakePlayerReady && !holder.world().isClientSide) {
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

        Inventory fakeInv = holder.getFakePlayer().getInventory();
        for (int slot = 1; slot < fakeInv.getContainerSize(); slot++) {
            ItemStack stack = fakeInv.getItem(slot);
            if (!stack.isEmpty()) {
                if (slot < useableSlots) {
                    // using super method here to avoid unnecessary copy of the item back to the fake player again
                    super.setStackInSlot(slot, stack);
                } else {
                    Vec3 v = holder.getDronePos();
                    PneumaticCraftUtils.dropItemOnGround(stack, holder.world(), v.x(), v.y(), v.z());
                }
                fakeInv.setItem(slot, ItemStack.EMPTY);
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
        if (slot >= fakePlayer.getInventory().items.size()) return;

        // not a copy: any changes to items in the fake player should also be reflected in the drone's item handler
        ItemStack newStack = getStackInSlot(slot);

        fakePlayer.getInventory().items.set(slot, newStack);
        if (slot == fakePlayer.getInventory().selected) {
            // currentItem is always 0 but we'll use that rather than a literal 0
            // maybe one day drones will be able to change their current held-item slot
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, newStack);
            if (!prevHeldStack.isEmpty()) {
                fakePlayer.getAttributes().removeAttributeModifiers(prevHeldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            }
            if (!newStack.isEmpty()) {
                fakePlayer.getAttributes().addTransientAttributeModifiers(newStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
            }
            prevHeldStack = newStack.copy();
        }
    }
}
