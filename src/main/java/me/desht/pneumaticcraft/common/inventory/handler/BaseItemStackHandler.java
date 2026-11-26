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

package me.desht.pneumaticcraft.common.inventory.handler;

import me.desht.pneumaticcraft.common.util.ValueIOBridge;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import javax.annotation.Nullable;

public class BaseItemStackHandler extends ItemStacksResourceHandler {
    protected final BlockEntity te;

    public BaseItemStackHandler(int size) {
        this(null, size);
    }

    public BaseItemStackHandler(BlockEntity te, int size) {
        super(size);
        this.te = te;
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previousContents) {
        if (te != null && te.getLevel() != null && !te.getLevel().isClientSide()) te.setChanged();
    }

    /**
     * {@return the stack in the given slot; this is the stored stack itself, not a copy, so don't modify it}
     */
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return isItemValid(slot, resource.toStack());
    }

    @Override
    protected int getCapacity(int slot, ItemResource resource) {
        return Math.min(super.getCapacity(slot, resource), getSlotLimit(slot));
    }

    /**
     * Override this to limit the number of items in each slot.
     *
     * @param slot the slot
     * @return the maximum number of items in the slot
     */
    public int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    /**
     * Override this to restrict which items may be inserted into each slot.
     *
     * @param slot the slot
     * @param stack the item, may be empty
     * @return true if the item is allowed in the slot
     */
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    /**
     * Change the number of slots; this clears the handler.
     */
    public void setSize(int size) {
        setStacks(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public ItemContainerContents toContainerContents() {
        return ItemContainerContents.fromItems(stacks);
    }

    public void loadContainerContents(@Nullable ItemContainerContents contents) {
        if (contents != null) {
            for (int i = 0; i < contents.getSlots() && i < stacks.size(); i++) {
                stacks.set(i, contents.getStackInSlot(i));
            }
            // in case the container contents are smaller...
            for (int i = contents.getSlots(); i < stacks.size(); i++) {
                stacks.set(i, ItemStack.EMPTY);
            }
        }
    }

    // Same format as the legacy ItemStackHandler, so existing saved inventories still load

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> itemList = output.list("Items", ItemStackWithSlot.CODEC);
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                itemList.add(new ItemStackWithSlot(i, stacks.get(i)));
            }
        }
        output.putInt("Size", stacks.size());
    }

    @Override
    public void deserialize(ValueInput input) {
        int size = input.getIntOr("Size", stacks.size());
        if (size != stacks.size()) {
            setStacks(NonNullList.withSize(size, ItemStack.EMPTY));
        } else {
            stacks.replaceAll(s -> ItemStack.EMPTY);
        }
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(slot -> {
            if (slot.isValidInContainer(stacks.size())) {
                stacks.set(slot.slot(), slot.stack());
            }
        });
    }

    // TODO 26.1 ValueInput/ValueOutput: temporary, see ValueIOBridge
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return ValueIOBridge.serialize(this, provider);
    }

    // TODO 26.1 ValueInput/ValueOutput: temporary, see ValueIOBridge
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ValueIOBridge.deserialize(this, provider, tag);
    }
}
