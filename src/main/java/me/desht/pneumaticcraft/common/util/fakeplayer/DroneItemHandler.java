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
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;


public class DroneItemHandler extends BaseItemStackHandler {
    private final IDrone holder;
    private int useableSlots;
    private ItemStack prevHeldStack = ItemStack.EMPTY;
    private boolean fakePlayerReady = false;

    public DroneItemHandler(IDrone holder, int useableSlots) {
        super(36);  // always has 36 slots to match a player main inv
        this.holder = holder;
        this.useableSlots = useableSlots;
    }

    @Override
    public int size() {
        return Math.min(useableSlots, super.size());
    }

    @Override
    public void set(int slot, ItemResource resource, int amount) {
        if (slot < useableSlots) {
            super.set(slot, resource, amount);
        }
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previousContents) {
        copyItemToFakePlayer(slot);
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
        if (!fakePlayerReady && !holder.getDroneLevel().isClientSide()) {
            fakePlayerReady = true;
            for (int slot = 0; slot < size(); slot++) {
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
                    // setting the stack directly here to avoid unnecessary copy of the item back to the fake player again
                    stacks.set(slot, stack);
                } else {
                    Vec3 v = holder.getDronePos();
                    PneumaticCraftUtils.dropItemOnGround(stack, holder.getDroneLevel(), v.x(), v.y(), v.z());
                }
                fakeInv.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Copy item from drone's ResourceHandler<ItemResource> inventory to the fake player's main inventory
     * Also handle item equipping where appropriate
     *
     * @param slot slot that is being updated
     */
    public void copyItemToFakePlayer(int slot) {
        if (!fakePlayerReady) return;

        FakePlayer fakePlayer = holder.getFakePlayer();

        // As it stands, drone inv can't go above 36 items (max 35 inv upgrades),
        // but let's be paranoid here
        if (slot >= fakePlayer.getInventory().getNonEquipmentItems().size()) return;

        // not a copy: any changes to items in the fake player should also be reflected in the drone's item handler
        ItemStack newStack = getStackInSlot(slot);

        fakePlayer.getInventory().getNonEquipmentItems().set(slot, newStack);
        if (slot == fakePlayer.getInventory().getSelectedSlot()) {
            // currentItem is always 0 but we'll use that rather than a literal 0
            // maybe one day drones will be able to change their current held-item slot
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, newStack);
            if (!prevHeldStack.isEmpty()) {
                prevHeldStack.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                    AttributeInstance instance = fakePlayer.getAttributes().getInstance(holder);
                    if (instance != null) {
                        instance.removeModifier(modifier);
                    }
                });
            }
            if (!newStack.isEmpty()) {
                fakePlayer.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                    AttributeInstance instance = fakePlayer.getAttributes().getInstance(holder);
                    if (instance != null) {
                        instance.removeModifier(modifier.id());
                        instance.addTransientModifier(modifier);
                    }
                });
            }
            prevHeldStack = newStack.copy();
        }
    }
}
