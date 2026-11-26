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

package me.desht.pneumaticcraft.common.inventory.slot;

import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A slot backed by an item handler; a port of the legacy {@code SlotItemHandler}. The item handler must be directly
 * modifiable, see {@link IOHelper#modifierFor(ResourceHandler)}.
 */
public class ItemHandlerSlot extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

    private final ResourceHandler<ItemResource> itemHandler;
    private final IndexModifier<ItemResource> modifier;
    protected final int index;

    public ItemHandlerSlot(ResourceHandler<ItemResource> itemHandler, int index, int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.modifier = IOHelper.modifierFor(itemHandler);
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return itemHandler.isValid(index, ItemResource.of(stack));
    }

    @Override
    public ItemStack getItem() {
        return IOHelper.getStack(itemHandler, index);
    }

    @Override
    public void set(ItemStack stack) {
        modifier.set(index, ItemResource.of(stack), stack.getCount());
        this.setChanged();
    }

    public void initialize(ItemStack stack) {
        modifier.set(index, ItemResource.of(stack), stack.getCount());
        this.setChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
    }

    @Override
    public int getMaxStackSize() {
        return itemHandler.getCapacityAsInt(index, ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), itemHandler.getCapacityAsInt(index, ItemResource.EMPTY));
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !IOHelper.extract(itemHandler, index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return IOHelper.extract(itemHandler, index, amount, false);
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof ItemHandlerSlot ihs && ihs.itemHandler == this.itemHandler;
    }
}
